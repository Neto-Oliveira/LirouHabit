package com.lirouhabit.app.monitor

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
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
        const val CHANNEL_STATUS = "monitor_status"
        const val CHANNEL_ALERT = "intervention_alert"
        private const val STATUS_NOTIF_ID = 1
        private const val ALERT_NOTIF_ID = 2
        private const val POLL_MS = 2_000L
        private const val MILESTONE_2_MIN = 15
        private const val MILESTONE_OVERLAY_MIN = 60
        private const val SNOOZE_AFTER_DEFEAT_MS = 5 * 60_000L
        private const val GRACE_MS = 2 * 60_000L // troca de aba não reseta a sessão

        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, MonitorService::class.java)
            )
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, MonitorService::class.java))
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
    private val firedMilestones = mutableMapOf<String, MutableSet<Int>>()
    private val cooldownUntil = mutableMapOf<String, Long>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val app = application as App
        db = app.db
        settings = app.settings
        usageMonitor = UsageMonitor(this)
        overlayManager = OverlayManager(this)
        startAsForeground()
        observeData()
        startLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
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
        scope.launch { settings.monitorEnabled.collect { monitorEnabled = it } }
        scope.launch { settings.escalationOn.collect { escalationOn = it } }
        scope.launch {
            db.trackedAppDao().all().collect { list ->
                trackedApps = list.filter { it.enabled }.associateBy { it.packageName }
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
        if (!monitorEnabled) {
            resetSession()
            return
        }
        // Notificações NÃO dependem de overlay — só do acesso ao uso.
        if (!UsageMonitor.hasUsageAccess(this)) {
            resetSession()
            return
        }

        val foreground = usageMonitor.currentForegroundApp() ?: return
        if (foreground == packageName) return // LirouHabit aberto: pausa, não reseta

        val now = System.currentTimeMillis()
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
        }
        accumulatedMs += POLL_MS
        lastSeenAt = now

        val fired = firedMilestones.getOrPut(foreground) { mutableSetOf() }
        val milestone = milestonesFor(tracked).firstOrNull {
            accumulatedMs >= it.minutes * 60_000L && it.minutes !in fired
        } ?: return

        if (milestone.isOverlay) {
            val cooldown = cooldownUntil[foreground] ?: 0L
            if (now < cooldown) return // snooze ativo — tenta de novo no próximo tick
        }

        fired.add(milestone.minutes)
        intervene(tracked, milestone)
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
    }

    private fun goHome() {
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_HOME)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { startActivity(intent) }
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
        }
    }
}
