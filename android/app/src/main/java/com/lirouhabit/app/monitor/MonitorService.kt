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
import com.lirouhabit.app.R
import com.lirouhabit.app.data.AppDatabase
import com.lirouhabit.app.data.DailyUsage
import com.lirouhabit.app.data.InterventionEvent
import com.lirouhabit.app.data.SettingsRepository
import com.lirouhabit.app.data.TrackedApp
import com.lirouhabit.app.data.WorkRoutine
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
 *  - limite de SESSÃO (default 5 min) → notificação reflexiva
 *  - 15 min ACUMULADOS no dia → notificação mais pesada
 *  - 60 min ACUMULADOS no dia → tela de bloqueio full-screen
 *  - cada nova entrada no vilão depois da 1h → confronto "Vale mesmo a pena?"
 *
 * Troca de aba/app: a sessão sobrevive por GRACE_MS (2 min). Saiu do vilão e
 * voltou rápido → o cronômetro CONTINUA de onde parou.
 */
class MonitorService : Service() {

    /** Um marco de tempo dentro de uma sessão contínua no app vilão. */
    private data class Milestone(
        val minutes: Int,
        val isOverlay: Boolean,
        val phraseLevel: Int,
        val isDaily: Boolean = false
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
        private const val SNOOZE_AFTER_DEFEAT_MS = 5 * 60_000L
        private const val GRACE_MS = 2 * 60_000L // troca de aba não reseta a sessão
        private const val GROUP_KEY = "__all_villains__"
        const val ACTION_FORCE_MILESTONE = "com.lirouhabit.app.FORCE_MILESTONE"

        // Estado compartilhado com o InterventionReceiver (vitória/derrota)
        val firedMilestones = mutableMapOf<String, MutableSet<Int>>()
        val dailyFiredMilestones = mutableMapOf<String, MutableSet<Int>>()
        val cooldownUntil = mutableMapOf<String, Long>()

        fun snooze(pkg: String, ms: Long) {
            cooldownUntil[pkg] = System.currentTimeMillis() + ms
        }

        fun resetMilestone(pkg: String, minutes: Int) {
            firedMilestones[pkg]?.remove(minutes)
            dailyFiredMilestones[pkg]?.remove(minutes)
        }

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
    @Volatile private var combineAppsTime = false
    @Volatile private var sessionAlertsOn = true
    @Volatile private var dailyWarningOn = true
    @Volatile private var blockingOn = true
    @Volatile private var blockAtSessionLimit = false
    @Volatile private var reentryOn = true
    @Volatile private var dailyOverlayMinutes = 60
    @Volatile private var workAlertsOn = true
    @Volatile private var workRoutines: List<WorkRoutine> = emptyList()
    @Volatile private var trackedApps: Map<String, TrackedApp> = emptyMap()
    @Volatile private var topDream: String = ""
    @Volatile private var topVice: String = ""
    @Volatile private var topHobby: String = ""
    @Volatile private var topHobbyLastAt: Long = 0L

    // Sessão atual (com graça de 2 min ao trocar de app)
    private var sessionPkg: String? = null
    private var accumulatedMs = 0L
    private var lastSeenAt = 0L
    private var lastTickAt = 0L
    // firedMilestones e cooldownUntil agora vivem no companion (acessíveis pelo receiver)

    // Diagnóstico (tela secreta de desenvolvimento)
    private var lastLoggedForeground: String? = null
    private var lastReentryForeground: String? = null
    private var lastPausedReason: String? = null
    private var lastLoggedMinute = 0
    private var lastStatusMinute = -1
    // Tracker diário
    private var dailyDate: String = ""
    private val dailyMs = mutableMapOf<String, Long>()
    private val lastPersistedMinute = mutableMapOf<String, Int>()
    private var lastWorkNagAt = 0L

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
        // Reidrata o acumulado diário ANTES de começar o loop: se o sistema matou
        // o serviço hoje, os marcos diários (15min/1h) continuam de onde pararam.
        scope.launch {
            hydrateDaily()
            startLoop()
        }
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
            .setSmallIcon(R.drawable.ic_bolt)
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

    @Volatile private var previousLimits: Map<String, Int> = emptyMap()

    private fun observeData() {
        scope.launch {
            settings.monitorEnabled.collect {
                monitorEnabled = it
                MonitorLog.add("monitor ligado=$it")
            }
        }
        scope.launch { settings.escalationOn.collect { escalationOn = it } }
        scope.launch { settings.sessionAlertsOn.collect { sessionAlertsOn = it } }
        scope.launch { settings.dailyWarningOn.collect { dailyWarningOn = it } }
        scope.launch { settings.blockingOn.collect { blockingOn = it } }
        scope.launch { settings.blockAtSessionLimit.collect { blockAtSessionLimit = it } }
        scope.launch { settings.reentryOn.collect { reentryOn = it } }
        scope.launch { settings.dailyOverlayMinutes.collect { dailyOverlayMinutes = it } }
        scope.launch { settings.workAlertsOn.collect { workAlertsOn = it } }
        scope.launch { db.workRoutineDao().all().collect { workRoutines = it } }
        scope.launch {
            settings.combineAppsTime.collect {
                combineAppsTime = it
                resetSession()
                MonitorLog.add("tempo acumulativo entre vilões=$it")
            }
        }
        scope.launch {
            db.trackedAppDao().all().collect { list ->
                val enabled = list.filter { it.enabled }
                // Se o limite de algum app mudou, reinicia a sessão desse app
                for (app in enabled) {
                    val old = previousLimits[app.packageName]
                    if (old != null && old != app.limitMinutes && sessionPkg == app.packageName) {
                        accumulatedMs = 0L
                        firedMilestones.remove(app.packageName)
                        lastLoggedMinute = 0
                        lastPersistedMinute.remove(app.packageName)
                        MonitorLog.add("limite alterado para ${app.limitMinutes}min — sessão reiniciada")
                    }
                }
                val currentLimits = enabled.associate { it.packageName to it.limitMinutes }
                // Mudou a lista/limite dos apps enquanto a soma estava ativa: nova sessão única.
                if (sessionPkg == GROUP_KEY && previousLimits != currentLimits) {
                    resetSession()
                }
                previousLimits = currentLimits
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
        scope.launch {
            db.hobbyDao().all().collect { list ->
                val hobby = list.firstOrNull()
                topHobby = hobby?.text.orEmpty()
                topHobbyLastAt = hobby?.lastPracticedAt ?: 0L
            }
        }
    }

    private fun todayIso(): String =
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())

    /** Recupera do banco o tempo acumulado de hoje (sobrevive a service kill). */
    private suspend fun hydrateDaily() {
        val today = todayIso()
        runCatching {
            val rows = db.dailyUsageDao().forDate(today)
            dailyDate = today
            dailyMs.clear()
            rows.forEach { dailyMs[it.packageName] = it.totalMs }
            MonitorLog.add("dia recuperado: ${rows.size} app(s) com tempo hoje")
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
        // Detecta NOVA entrada (serve para o confronto pós-1h ao voltar ao vilão)
        val enteringNow = foreground != lastReentryForeground
        lastReentryForeground = foreground
        if (foreground == packageName) return // LirouHabit aberto: pausa, não reseta

        val tracked = trackedApps[foreground]

        if (tracked == null) {
            // Fora do vilão: a sessão sobrevive durante a graça (troca rápida de aba)
            if (sessionPkg != null && now - lastSeenAt > GRACE_MS) {
                resetSession()
            }
            return
        }

        // Modo acumulativo: todos os vilões viram UM único alvo lógico.
        // Trocar Instagram → TikTok mantém sessão, dia, marcos e cooldown.
        val aggregate = combineAppsTime && trackedApps.size > 1
        val subjectKey = if (aggregate) GROUP_KEY else foreground
        val subject = if (aggregate) {
            tracked.copy(
                packageName = GROUP_KEY,
                appName = "Apps vilões (soma)",
                limitMinutes = trackedApps.values.minOf { it.limitMinutes }
            )
        } else tracked

        if (sessionPkg != subjectKey) {
            // Mudou de alvo (ou graça expirada): sessão nova
            accumulatedMs = 0L
            firedMilestones.remove(subjectKey)
            sessionPkg = subjectKey
            lastLoggedMinute = 0
        }
        accumulatedMs += elapsed
        lastSeenAt = now
        sessionApp = subject.appName
        sessionMs = accumulatedMs

        // Tracker diário continua individual para os gráficos.
        val today = todayIso()
        if (dailyDate != today) {
            dailyDate = today
            dailyMs.clear()
            lastPersistedMinute.clear()
            dailyFiredMilestones.clear() // novo dia: marcos diários podem rolar de novo
        }
        val prevDaily = dailyMs[tracked.packageName] ?: 0L
        val newDaily = prevDaily + elapsed
        dailyMs[tracked.packageName] = newDaily
        val persistedMin = lastPersistedMinute[tracked.packageName] ?: -1
        val newMin = (newDaily / 60_000L).toInt()
        if (newMin > persistedMin) {
            lastPersistedMinute[tracked.packageName] = newMin
            scope.launch {
                runCatching {
                    db.dailyUsageDao().upsert(
                        DailyUsage(
                            date = today,
                            packageName = tracked.packageName,
                            appName = tracked.appName,
                            totalMs = newDaily
                        )
                    )
                }
            }
        }

        // No modo acumulativo, o tempo de todos os apps habilitados alimenta os marcos.
        val subjectDailyMs = if (aggregate) {
            trackedApps.keys.sumOf { dailyMs[it] ?: 0L }
        } else newDaily

        // Prova visual de que o tempo está acumulando (sessão e dia)
        val wholeMinutes = (accumulatedMs / 60_000L).toInt()
        if (wholeMinutes > lastLoggedMinute) {
            lastLoggedMinute = wholeMinutes
            MonitorLog.add("acumulado sessão: ${wholeMinutes}min em ${subject.appName}")
            updateStatusNotification(
                "${subject.appName} • sessão ${"%.1f".format(accumulatedMs / 60_000.0)} / ${subject.limitMinutes} min" +
                    " · dia ${"%.0f".format(subjectDailyMs / 60_000.0)} min"
            )
        }

        // Extra de ROTINA DE TRABALHO: durante a janela, 1 alerta por minuto no vilão.
        if (workAlertsOn && !overlayManager.isShowing) {
            val localTime = java.time.LocalTime.now()
            val minuteOfDay = localTime.hour * 60 + localTime.minute
            val routine = workRoutines.firstOrNull {
                it.isActiveAt(java.time.LocalDate.now().dayOfWeek.value, minuteOfDay)
            }
            if (routine != null && now - lastWorkNagAt >= 60_000L) {
                lastWorkNagAt = now
                fireWorkNotification(tracked, routine)
                return // nunca duplica com outro marco no mesmo tick
            }
        }

        // 1) Marco de SESSÃO (limite configurável): notificação ou tela de bloqueio.
        val sessionFired = firedMilestones.getOrPut(subjectKey) { mutableSetOf() }
        val sessionCooldown = cooldownUntil[subjectKey] ?: 0L
        if (
            (sessionAlertsOn || blockAtSessionLimit) &&
            subject.limitMinutes !in sessionFired &&
            accumulatedMs >= subject.limitMinutes * 60_000L &&
            now >= sessionCooldown
        ) {
            sessionFired.add(subject.limitMinutes)
            MonitorLog.add(
                "marco de sessão ${subject.limitMinutes}min em ${subject.appName} -> " +
                    if (blockAtSessionLimit) "tela de bloqueio" else "notificação"
            )
            intervene(
                subject,
                Milestone(
                    subject.limitMinutes,
                    isOverlay = blockAtSessionLimit,
                    phraseLevel = if (blockAtSessionLimit) 3 else 1,
                    isDaily = false
                )
            )
            if (blockAtSessionLimit) return
        }

        // 2) Marcos DIÁRIOS acumulativos: não resetam ao trocar de aba/app.
        val dailyFired = dailyFiredMilestones.getOrPut(subjectKey) { mutableSetOf() }
        val dailyMilestones = buildList {
            if (dailyWarningOn && MILESTONE_2_MIN < dailyOverlayMinutes) {
                add(Milestone(MILESTONE_2_MIN, false, 2, isDaily = true))
            }
            if (blockingOn) {
                add(Milestone(dailyOverlayMinutes, true, 3, isDaily = true))
            }
        }
        for (milestone in dailyMilestones) {
            if (milestone.minutes <= subject.limitMinutes) continue // só acima do limite de sessão
            if (subjectDailyMs < milestone.minutes * 60_000L || milestone.minutes in dailyFired) continue
            if (milestone.isOverlay) {
                val cooldown = cooldownUntil[subjectKey] ?: 0L
                if (now < cooldown) continue // snooze ativo — tenta no próximo tick
            }
            dailyFired.add(milestone.minutes)
            MonitorLog.add(
                "marco DIÁRIO ${milestone.minutes}min em ${subject.appName} -> " +
                    if (milestone.isOverlay) "tela de bloqueio" else "notificação"
            )
            intervene(subject, milestone)
            return // um marco por tick; reentrada só acontece se nada disparou agora
        }

        // 3) Depois de 1h no dia: TODA NOVA ENTRADA no vilão traz de volta a tela
        // de bloqueio, agora com uma pergunta direta ("Vale mesmo a pena?").
        val dailyMinutes = (subjectDailyMs / 60_000L).toInt()
        val cooldown = cooldownUntil[subjectKey] ?: 0L
        if (
            enteringNow &&
            reentryOn && blockingOn &&
            dailyMinutes >= dailyOverlayMinutes &&
            !overlayManager.isShowing &&
            now >= cooldown
        ) {
            MonitorLog.add("reentrada pós-1h em ${subject.appName} -> confronto")
            intervene(
                subject,
                Milestone(dailyMinutes, isOverlay = true, phraseLevel = 4, isDaily = true)
            )
        }
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

    private fun intervene(app: TrackedApp, milestone: Milestone) {
        val level = if (escalationOn) milestone.phraseLevel else 1
        val phrase = PhraseBank.phrase(
            level = level,
            dream = topDream,
            vice = topVice,
            app = app.appName,
            minutes = milestone.minutes,
            hobby = topHobby,
            hobbyLastAt = topHobbyLastAt
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
            if (milestone.isDaily) {
                dailyFiredMilestones[app.packageName]?.remove(milestone.minutes)
            } else {
                firedMilestones[app.packageName]?.remove(milestone.minutes)
            }
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
                .setSmallIcon(R.drawable.ic_bolt)
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

    /** Alerta extra de rotina de trabalho (1x/min enquanto o vilão está aberto). */
    private fun fireWorkNotification(app: TrackedApp, routine: WorkRoutine) {
        val dreamTail = if (topDream.isBlank()) "" else " Seu foco: \"$topDream\"."
        val phrase = "Ei, você não devia estar aqui — hora do trabalho. " +
            "${app.appName} pode esperar.$dreamTail"
        MonitorLog.add("trabalho (${routine.label}): alerta em ${app.appName}")
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
        fireAlertNotification(
            app = app,
            phrase = phrase,
            minutes = 0,
            title = "Hora do trabalho • ${routine.label}"
        )
    }

    private fun fireAlertNotification(
        app: TrackedApp,
        phrase: String,
        minutes: Int,
        title: String = "${app.appName} • $minutes min"
    ) {
        val closeIntent = Intent(this, InterventionReceiver::class.java).apply {
            action = InterventionReceiver.ACTION_CLOSE_APP
            putExtra(InterventionReceiver.EXTRA_PKG, app.packageName)
            putExtra(InterventionReceiver.EXTRA_APP, app.appName)
            putExtra(InterventionReceiver.EXTRA_PHRASE, phrase)
            putExtra(InterventionReceiver.EXTRA_MINUTES, minutes)
        }
        val ignoreIntent = Intent(this, InterventionReceiver::class.java).apply {
            action = InterventionReceiver.ACTION_IGNORE
            putExtra(InterventionReceiver.EXTRA_PKG, app.packageName)
            putExtra(InterventionReceiver.EXTRA_APP, app.appName)
            putExtra(InterventionReceiver.EXTRA_PHRASE, phrase)
            putExtra(InterventionReceiver.EXTRA_MINUTES, minutes)
        }
        val dismissIntent = Intent(this, InterventionReceiver::class.java).apply {
            action = InterventionReceiver.ACTION_DISMISS
            putExtra(InterventionReceiver.EXTRA_PKG, app.packageName)
            putExtra(InterventionReceiver.EXTRA_APP, app.appName)
            putExtra(InterventionReceiver.EXTRA_PHRASE, phrase)
            putExtra(InterventionReceiver.EXTRA_MINUTES, minutes)
        }

        val closePi = PendingIntent.getBroadcast(
            this, app.packageName.hashCode() + 1, closeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val ignorePi = PendingIntent.getBroadcast(
            this, app.packageName.hashCode() + 2, ignoreIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val dismissPi = PendingIntent.getBroadcast(
            this, app.packageName.hashCode() + 3, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ALERT)
            .setSmallIcon(R.drawable.ic_bolt)
            .setContentTitle(title)
            .setContentText(phrase)
            .setStyle(NotificationCompat.BigTextStyle().bigText(phrase))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setDeleteIntent(dismissPi)
            .addAction(R.drawable.ic_lock, "Fechar o app", closePi)
            .addAction(R.drawable.ic_chain, "Ignorar", ignorePi)
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
