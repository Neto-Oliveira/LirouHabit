package com.lirouhabit.app.ui.vm

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lirouhabit.app.data.Dream
import com.lirouhabit.app.data.DailyUsage
import com.lirouhabit.app.data.InterventionEvent
import com.lirouhabit.app.data.SettingsRepository
import com.lirouhabit.app.data.TrackedApp
import com.lirouhabit.app.data.Vice
import com.lirouhabit.app.R
import com.lirouhabit.app.lirouApp
import com.lirouhabit.app.monitor.MonitorLog
import com.lirouhabit.app.monitor.MonitorService
import com.lirouhabit.app.monitor.OverlayManager
import com.lirouhabit.app.monitor.PhraseBank
import com.lirouhabit.app.monitor.UsageMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class InstalledApp(
    val packageName: String,
    val name: String,
    val icon: ImageBitmap?
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = application.lirouApp.db
    private val settings = application.lirouApp.settings
    private val usageMonitor = UsageMonitor(application)

    init {
        // Migração: quem concluiu o onboarding ANTES do auto-ligar existir ficava
        // com o monitor desligado para sempre. Liga uma única vez automaticamente.
        viewModelScope.launch {
            val done = settings.isOnboardingDoneNow()
            val toggled = settings.wasMonitorToggledOnce()
            if (done && !toggled) {
                setMonitor(true)
            }
            ensureMonitorRunning()
        }
    }

    private fun <T> kotlinx.coroutines.flow.Flow<T>.asState(initial: T): StateFlow<T> =
        stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)

    val dreams: StateFlow<List<Dream>> = db.dreamDao().all().asState(emptyList())
    val vices: StateFlow<List<Vice>> = db.viceDao().all().asState(emptyList())
    val trackedApps: StateFlow<List<TrackedApp>> = db.trackedAppDao().all().asState(emptyList())
    val dailyUsage: StateFlow<List<DailyUsage>> = db.dailyUsageDao().all().asState(emptyList())
    val events: StateFlow<List<InterventionEvent>> = db.eventDao().recent().asState(emptyList())
    val victories: StateFlow<Int> = db.eventDao().victories().asState(0)
    val defeats: StateFlow<Int> = db.eventDao().defeats().asState(0)
    val triggers: StateFlow<Int> = db.eventDao().triggers().asState(0)

    val defaultLimit: StateFlow<Int> = settings.defaultLimit.asState(SettingsRepository.DEFAULT_LIMIT)
    val monitorEnabled: StateFlow<Boolean> = settings.monitorEnabled.asState(false)
    val onboardingDone: StateFlow<Boolean> = settings.onboardingDone.asState(false)
    val escalationOn: StateFlow<Boolean> = settings.escalationOn.asState(true)

    // ---------- Sonhos ----------
    fun saveDream(id: Long = 0, text: String, priority: Int) = viewModelScope.launch {
        if (text.isNotBlank()) {
            db.dreamDao().upsert(Dream(id = id, text = text.trim(), priority = priority))
        }
    }

    fun deleteDream(dream: Dream) = viewModelScope.launch { db.dreamDao().delete(dream) }

    // ---------- Vícios / maus hábitos ----------
    fun saveVice(id: Long = 0, text: String) = viewModelScope.launch {
        if (text.isNotBlank()) {
            db.viceDao().upsert(Vice(id = id, text = text.trim()))
        }
    }

    fun deleteVice(vice: Vice) = viewModelScope.launch { db.viceDao().delete(vice) }

    // ---------- Apps vilões ----------
    fun addTrackedApp(packageName: String, appName: String) = viewModelScope.launch {
        db.trackedAppDao().upsert(
            TrackedApp(
                packageName = packageName,
                appName = appName,
                limitMinutes = defaultLimit.value
            )
        )
    }

    fun removeTrackedApp(app: TrackedApp) = viewModelScope.launch {
        db.trackedAppDao().delete(app)
    }

    fun setTrackedAppLimit(app: TrackedApp, minutes: Int) = viewModelScope.launch {
        db.trackedAppDao().upsert(app.copy(limitMinutes = minutes.coerceIn(1, 120)))
    }

    fun setTrackedAppEnabled(app: TrackedApp, enabled: Boolean) = viewModelScope.launch {
        db.trackedAppDao().upsert(app.copy(enabled = enabled))
    }

    suspend fun loadInstalledApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val ctx = getApplication<Application>()
        val pm = ctx.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        pm.queryIntentActivities(intent, 0)
            .map { ri ->
                InstalledApp(
                    packageName = ri.activityInfo.packageName,
                    name = ri.loadLabel(pm).toString(),
                    icon = runCatching {
                        ri.loadIcon(pm).toBitmap(width = 96, height = 96).asImageBitmap()
                    }.getOrNull()
                )
            }
            .distinctBy { it.packageName }
            .filter { it.packageName != ctx.packageName }
            .sortedBy { it.name.lowercase() }
    }

    // ---------- Configurações ----------
    fun setDefaultLimit(value: Int) = viewModelScope.launch { settings.setDefaultLimit(value) }
    fun setEscalation(value: Boolean) = viewModelScope.launch { settings.setEscalationOn(value) }

    fun setMonitor(on: Boolean) = viewModelScope.launch {
        settings.setMonitorToggledOnce(true)
        settings.setMonitorEnabled(on)
        val ctx = getApplication<Application>()
        if (on) MonitorService.start(ctx) else MonitorService.stop(ctx)
    }

    /** Conclui o onboarding: marca como feito e já LIGA o monitor automaticamente. */
    fun finishOnboarding() = viewModelScope.launch {
        settings.setOnboardingDone(true)
        settings.setMonitorEnabled(true)
        MonitorService.start(getApplication())
    }
    fun redoOnboarding() = viewModelScope.launch { settings.setOnboardingDone(false) }

    // ---------- Permissões ----------
    var permUsage by mutableStateOf(false); private set
    var permOverlay by mutableStateOf(false); private set
    var permNotif by mutableStateOf(false); private set
    var permBattery by mutableStateOf(false); private set

    /** Diagnóstico: app em foreground agora — prova de que o acesso ao uso funciona. */
    fun detectForegroundNow(): String =
        runCatching { usageMonitor.currentForegroundApp() ?: "nada detectado" }
            .getOrDefault("erro ao ler (permissão?)")

    /** Dispara o alerta REAL na hora (som + vibração) — teste de ponta a ponta da notificação. */
    fun testAlert(level: Int = 1, minutes: Int = 5) {
        val ctx = getApplication<Application>()
        val phrase = PhraseBank.phrase(
            level = level,
            dream = dreams.value.firstOrNull()?.text ?: "",
            vice = vices.value.firstOrNull()?.text ?: "",
            app = "Instagram",
            minutes = minutes
        )
        val notification = NotificationCompat.Builder(ctx, MonitorService.CHANNEL_ALERT)
            .setSmallIcon(R.drawable.ic_bolt)
            .setContentTitle("Instagram • $minutes min (TESTE)")
            .setContentText(phrase)
            .setStyle(NotificationCompat.BigTextStyle().bigText(phrase))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()
        runCatching { NotificationManagerCompat.from(ctx).notify(900, notification) }
            .onSuccess { MonitorLog.add("TESTE: notificação enviada (${minutes}min)") }
            .onFailure { MonitorLog.add("TESTE: FALHA na notificação: ${it.message}") }
    }

    /** Reabre o serviço do monitor se ele foi morto pelo sistema (ex.: Samsung). */
    fun ensureMonitorRunning() = viewModelScope.launch {
        if (settings.isMonitorEnabledNow() && !MonitorService.isRunning) {
            MonitorService.start(getApplication())
            MonitorLog.add("serviço reiniciado (estava morto)")
        }
    }

    /** Dispara um marco pelo CAMINHO DO SERVIÇO (não pelo VM) — prova ponta a ponta. */
    fun forceMilestone() {
        MonitorService.forceMilestone(getApplication())
    }
    /** Abre a tela de bloqueio REAL na hora — teste de ponta a ponta do overlay. */
    fun testOverlay() {
        MonitorLog.add("TESTE: abrindo tela de bloqueio")
        OverlayManager(getApplication()).show(
            phrase = "TESTE: 1 hora aqui. Não foi tempo que passou — foi a parte de hoje que o seu sonho não recebeu.",
            appName = "Instagram (teste)",
            dream = dreams.value.firstOrNull()?.text ?: "Terminar a faculdade",
            elapsedMinutes = 60
        ) { }
    }

    fun refreshPermissions() {
        val ctx = getApplication<Application>()
        permUsage = UsageMonitor.hasUsageAccess(ctx)
        permOverlay = Settings.canDrawOverlays(ctx)
        permNotif = Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        val power = ctx.getSystemService(PowerManager::class.java)
        permBattery = power?.isIgnoringBatteryOptimizations(ctx.packageName) ?: true
    }

    val corePermissionsGranted: Boolean
        get() = permUsage && permOverlay && permNotif
}
