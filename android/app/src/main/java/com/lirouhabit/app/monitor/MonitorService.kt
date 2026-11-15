package com.lirouhabit.app.monitor

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.lirouhabit.app.App
import com.lirouhabit.app.MainActivity
import com.lirouhabit.app.data.AppDatabase
import com.lirouhabit.app.data.InterventionEvent
import com.lirouhabit.app.data.SettingsRepository
import com.lirouhabit.app.data.TrackedApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Serviço em foreground que cronometra o uso dos apps vilões.
 *
 * Marcos por sessão (a intervenção escala — sem tela cheia no começo):
 *  - limite do app (default 5 min) → notificação reflexiva
 *  - 15 min → notificação mais pesada
 *  - 60 min → tela de bloqueio full-screen por cima do app
 *
 * Troca de aba/app: a sessão sobrevive por GRACE_MS (2 min). Saiu do vilão e
 * voltou rápido → o cronômetro CONTINUA de onde parou.
 */
class MonitorService : Service() {

    /** Um marco de tempo dentro de uma sessão contínua no app vilão. */
    private data class Milestone(
        val minutes: Int,
        val isOverlay: Boolean,
        val phraseLevel: Int
    )

    companion object {
        /** Flag simples para a UI saber se o serviço está rodando. */
        @Volatile
        var isRunning = false
            private set

        /** Sessão atual exposta para a tela de teste (nome do app + ms acumulados). */
        @Volatile
        var sessionApp: String? = null

        @Volatile
        var sessionMs = 0L

        const val CHANNEL_STATUS = "monitor_status"
        // v2: ID novo força a recriação do canal em instalações antigas
        // (som/vibração de canal ficam travados após a criação no Android 8+)
        const val CHANNEL_ALERT = "intervention_alert_v2"
        private const val STATUS_NOTIF_ID = 1
        private const val ALERT_NOTIF_ID = 2
        private const val POLL_MS = 2_000L
        private const val MILESTONE_2_MIN = 15
        private const val MILESTONE_OVERLAY_MIN = 60
        private const val SNOOZE_AFTER_DEFEAT_MS = 5 * 60_000L
        private const val GRACE_MS = 2 * 60_000L // troca de aba não reseta a sessão
        const val ACTION_FORCE_MILESTONE = "com.lirouhabit.app.FORCE_MILESTONE"

        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, MonitorService::class.java)
            )
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, MonitorService::class.java))
        }

        /** Força um marco agora (usado pela tela de teste para provar o caminho do serviço). */
        fun forceMilestone(context: Context) {
            context.startService(
                Intent(context, MonitorService::class.java).setAction(ACTION_FORCE_MILESTONE)
            )
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var usageMonitor: UsageMonitor
    private lateinit var overlayManager: OverlayManager
    private lateinit var db: AppDatabase
    private lateinit var settings: SettingsRepository

    @Volatile private var monitorEnabled = false
    @Volatile private var escalationOn = true
    @Volatile private var trackedApps: Map<String, TrackedApp> = emptyMap()
    @Volatile private var topDream: String = ""
    @Volatile private var topVice: String = ""

    // Sessão atual (com graça de 2 min ao trocar de app)
    private var sessionPkg: String? = null
    private var accumulatedMs = 0L
    private var lastSeenAt = 0L
    private var lastTickAt = 0L
    private val firedMilestones = mutableMapOf<String, MutableSet<Int>>()
    private val cooldownUntil = mutableMapOf<String, Long>()

    // Diagnóstico (tela secreta de desenvolvimento)
    private var lastLoggedForeground: String? = null
    private var lastPausedReason: String? = null
    private var lastLoggedMinute = 0
    private var lastStatusMinute = -1

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        MonitorLog.add("serviço iniciado")
        val app = application as App
        db = app.db
        settings = app.settings
        usageMonitor = UsageMonitor(this)
        overlayManager = OverlayManager(this)
        startAsForeground()
        observeData()
        startLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_FORCE_MILESTONE) {
            scope.launch { forceMilestone() }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        MonitorLog.add("serviço encerrado")
        overlayManager.hide()
        scope.cancel()
        super.onDestroy()
    }

    private fun startAsForeground() {
        val notification = NotificationCompat.Builder(this, CHANNEL_STATUS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("LirouHabit vigiando")
            .setContentText("Monitor anti-procrastinação ativo")
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                STATUS_NOTIF_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(STATUS_NOTIF_ID, notification)
        }
    }

    private fun observeData() {
        scope.launch {
            settings.monitorEnabled.collect {
                monitorEnabled = it
                MonitorLog.add("monitor ligado=$it")
            }
        }
        scope.launch { settings.escalationOn.collect { escalationOn = it } }
        scope.launch {
            db.trackedAppDao().all().collect { list ->
                val enabled = list.filter { it.enabled }
                trackedApps = enabled.associateBy { it.packageName }
                MonitorLog.add(
                    "apps vilões (${enabled.size}): " +
                        enabled.joinToString { "${it.appName}=${it.limitMinutes}min" }
                )
            }
        }
        scope.launch {
            db.dreamDao().all().collect { list ->
                topDream = list.firstOrNull()?.text.orEmpty()
            }
        }
        scope.launch {
            db.viceDao().all().collect { list ->
                topVice = list.firstOrNull()?.text.orEmpty()
            }
        }
    }

    private fun startLoop() {
        scope.launch {
            while (isActive) {
                runCatching { tick() }
                delay(POLL_MS)
            }
        }
    }

    private fun tick() {
        val now = System.currentTimeMillis()
        val elapsed = if (lastTickAt == 0L) POLL_MS else (now - lastTickAt).coerceIn(0L, POLL_MS * 3)
        lastTickAt = now

        if (!monitorEnabled) {
            pause("monitor desligado")
            resetSession()
            return
        }
        // Notificações NÃO dependem de overlay — só do acesso ao uso.
        if (!UsageMonitor.hasUsageAccess(this)) {
            pause("sem acesso ao uso")
            resetSession()
            return
        }
        resume()

        val foreground = usageMonitor.currentForegroundApp() ?: return
        if (foreground != lastLoggedForeground) {
            lastLoggedForeground = foreground
            MonitorLog.add("foreground: $foreground")
        }
        if (foreground == packageName) return // LirouHabit aberto: pausa, não reseta

        val tracked = trackedApps[foreground]

        if (tracked == null) {
            // Fora do vilão: a sessão sobrevive durante a graça (troca rápida de aba)
            if (sessionPkg != null && now - lastSeenAt > GRACE_MS) {
                resetSession()
            }
            return
        }

        if (sessionPkg != foreground) {
            // Vilão diferente (ou graça expirada): sessão nova
            accumulatedMs = 0L
            firedMilestones.remove(foreground)
            sessionPkg = foreground
            lastLoggedMinute = 0
        }
        accumulatedMs += elapsed
        lastSeenAt = now
        sessionApp = tracked.appName
        sessionMs = accumulatedMs

        // Prova visual de que o tempo está acumulando (mesmo antes do marco)
        val wholeMinutes = (accumulatedMs / 60_000L).toInt()
        if (wholeMinutes > lastLoggedMinute) {
            lastLoggedMinute = wholeMinutes
            MonitorLog.add("acumulado: ${wholeMinutes}min em ${tracked.appName}")
            updateStatusNotification("${tracked.appName} • ${"%.1f".format(accumulatedMs / 60_000.0)} / ${tracked.limitMinutes} min")
        }

        val fired = firedMilestones.getOrPut(foreground) { mutableSetOf() }
        val milestone = milestonesFor(tracked).firstOrNull {
            accumulatedMs >= it.minutes * 60_000L && it.minutes !in fired
        } ?: return

        if (milestone.isOverlay) {
            val cooldown = cooldownUntil[foreground] ?: 0L
            if (now < cooldown) return // snooze ativo — tenta de novo no próximo tick
        }

        fired.add(milestone.minutes)
        MonitorLog.add(
            "marco ${milestone.minutes}min em ${tracked.appName} -> " +
                if (milestone.isOverlay) "tela de bloqueio" else "notificação"
        )
        intervene(tracked, milestone)
    }

    private fun pause(reason: String) {
        if (lastPausedReason != reason) {
            lastPausedReason = reason
            MonitorLog.add("pausado: $reason")
        }
    }

    private fun resume() {
        if (lastPausedReason != null) {
            lastPausedReason = null
            MonitorLog.add("monitor rodando (uso + apps ok)")
        }
    }

    /**
     * Marcos da sessão: limite do app → notificação, 15 min → notificação pesada,
     * 60 min → overlay full-screen. Marcos menores que o limite do app são ignorados.
     */
    private fun milestonesFor(app: TrackedApp): List<Milestone> {
        val raw = buildList {
            add(app.limitMinutes to false)
            if (MILESTONE_2_MIN > app.limitMinutes) add(MILESTONE_2_MIN to false)
            if (MILESTONE_OVERLAY_MIN > app.limitMinutes) add(MILESTONE_OVERLAY_MIN to true)
        }
        return raw.mapIndexed { index, (min, overlay) ->
            Milestone(
                minutes = min,
                isOverlay = overlay,
                phraseLevel = if (overlay) 3 else (index + 1).coerceAtMost(2)
            )
        }
    }

    private fun intervene(app: TrackedApp, milestone: Milestone) {
        val level = if (escalationOn) milestone.phraseLevel else 1
        val phrase = PhraseBank.phrase(
            level = level,
            dream = topDream,
            vice = topVice,
            app = app.appName,
            minutes = milestone.minutes
        )

        scope.launch {
            db.eventDao().insert(
                InterventionEvent(
                    packageName = app.packageName,
                    appName = app.appName,
                    outcome = "TRIGGERED",
                    phrase = phrase
                )
            )
        }

        if (milestone.isOverlay) {
            vibrateStrong()
            overlayManager.show(
                phrase = phrase,
                appName = app.appName,
                dream = topDream,
                elapsedMinutes = milestone.minutes
            ) { victory ->
                handleOutcome(app, milestone, victory, phrase)
            }
        } else {
            fireAlertNotification(app, phrase, milestone.minutes)
        }
    }

    private fun handleOutcome(
        app: TrackedApp,
        milestone: Milestone,
        victory: Boolean,
        phrase: String
    ) {
        if (victory) {
            goHome()
        } else {
            // Derrota: snooze de 5 min e a tela de bloqueio volta
            cooldownUntil[app.packageName] =
                System.currentTimeMillis() + SNOOZE_AFTER_DEFEAT_MS
            firedMilestones[app.packageName]?.remove(milestone.minutes)
        }
        scope.launch {
            db.eventDao().insert(
                InterventionEvent(
                    packageName = app.packageName,
                    appName = app.appName,
                    outcome = if (victory) "VICTORY" else "DEFEAT",
                    phrase = phrase
                )
            )
        }
    }

    private fun resetSession() {
        sessionPkg = null
        accumulatedMs = 0L
        lastSeenAt = 0L
        lastLoggedMinute = 0
        lastStatusMinute = -1
        lastTickAt = 0L
        sessionApp = null
        sessionMs = 0L
        updateStatusNotification()
    }

    private fun goHome() {
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_HOME)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { startActivity(intent) }
    }

    /** Atualiza a notificação persistente com o progresso da sessão atual. */
    private fun updateStatusNotification(text: String = "Monitor anti-procrastinação ativo") {
        runCatching {
            val notification = NotificationCompat.Builder(this, CHANNEL_STATUS)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("LirouHabit vigiando")
                .setContentText(text)
                .setContentIntent(
                    PendingIntent.getActivity(
                        this, 0,
                        Intent(this, MainActivity::class.java),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
                .setOngoing(true)
                .build()
            NotificationManagerCompat.from(this).notify(STATUS_NOTIF_ID, notification)
        }
    }

    /** Força um marco agora (caminho do serviço) — usado pela tela de teste. */
    private fun forceMilestone() {
        val app = trackedApps.values.firstOrNull()
        if (app == null) {
            MonitorLog.add("TESTE (serviço): nenhum app vilão cadastrado")
            return
        }
        MonitorLog.add("TESTE (serviço): forçando marco em ${app.appName}")
        intervene(app, Milestone(app.limitMinutes, false, 1))
    }

    /** Vibração forte e característica ao exibir a tela de bloqueio (marco de 1h). */
    private fun vibrateStrong() {
        runCatching {
            val pattern = longArrayOf(0, 500, 150, 500, 150, 900)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(VibratorManager::class.java)
                vm.defaultVibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Vibrator::class.java)
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            }
        }
    }

    private fun fireAlertNotification(app: TrackedApp, phrase: String, minutes: Int) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ALERT)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("${app.appName} • $minutes min")
            .setContentText(phrase)
            .setStyle(NotificationCompat.BigTextStyle().bigText(phrase))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()
        runCatching {
            NotificationManagerCompat.from(this).notify(ALERT_NOTIF_ID, notification)
        }.onSuccess {
            MonitorLog.add("notificação enviada (${app.appName}, ${minutes}min)")
        }.onFailure {
            MonitorLog.add("FALHA na notificação: ${it.message}")
        }
    }
}
