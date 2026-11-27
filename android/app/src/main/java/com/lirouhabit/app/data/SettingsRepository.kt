package com.lirouhabit.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "lirouhabit_settings")

class SettingsRepository(private val context: Context) {

    companion object {
        private val KEY_DEFAULT_LIMIT = intPreferencesKey("default_limit_minutes")
        private val KEY_MONITOR_ON = booleanPreferencesKey("monitor_enabled")
        private val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        private val KEY_ESCALATION = booleanPreferencesKey("escalation_on")
        private val KEY_MONITOR_TOGGLED_ONCE = booleanPreferencesKey("monitor_toggled_once")
        private val KEY_COMBINE_APPS = booleanPreferencesKey("combine_apps_time")
        private val KEY_SESSION_ALERTS = booleanPreferencesKey("session_alerts_on")
        private val KEY_DAILY_WARNING = booleanPreferencesKey("daily_warning_on")
        private val KEY_BLOCKING = booleanPreferencesKey("blocking_on")
        private val KEY_BLOCK_AT_SESSION = booleanPreferencesKey("block_at_session_limit")
        private val KEY_REENTRY = booleanPreferencesKey("reentry_on")
        private val KEY_DAILY_LIMIT = intPreferencesKey("daily_overlay_minutes")
        private val KEY_WORK_ALERTS = booleanPreferencesKey("work_alerts_on")
        const val DEFAULT_LIMIT = 5
        const val DEFAULT_DAILY_LIMIT = 60
    }

    val defaultLimit: Flow<Int> =
        context.dataStore.data.map { it[KEY_DEFAULT_LIMIT] ?: DEFAULT_LIMIT }

    val monitorEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[KEY_MONITOR_ON] ?: false }

    val onboardingDone: Flow<Boolean> =
        context.dataStore.data.map { it[KEY_ONBOARDING_DONE] ?: false }

    val escalationOn: Flow<Boolean> =
        context.dataStore.data.map { it[KEY_ESCALATION] ?: true }

    /** Soma todos os apps vilões como um único balde de tempo (sem "trocar de refúgio"). */
    val combineAppsTime: Flow<Boolean> =
        context.dataStore.data.map { it[KEY_COMBINE_APPS] ?: false }

    val sessionAlertsOn: Flow<Boolean> =
        context.dataStore.data.map { it[KEY_SESSION_ALERTS] ?: true }
    val dailyWarningOn: Flow<Boolean> =
        context.dataStore.data.map { it[KEY_DAILY_WARNING] ?: true }
    val blockingOn: Flow<Boolean> =
        context.dataStore.data.map { it[KEY_BLOCKING] ?: true }
    val blockAtSessionLimit: Flow<Boolean> =
        context.dataStore.data.map { it[KEY_BLOCK_AT_SESSION] ?: false }
    val reentryOn: Flow<Boolean> =
        context.dataStore.data.map { it[KEY_REENTRY] ?: true }
    val dailyOverlayMinutes: Flow<Int> =
        context.dataStore.data.map { it[KEY_DAILY_LIMIT] ?: DEFAULT_DAILY_LIMIT }
    val workAlertsOn: Flow<Boolean> =
        context.dataStore.data.map { it[KEY_WORK_ALERTS] ?: true }

    /** Se o usuário já ligou/desligou o monitor manualmente alguma vez. */
    val monitorToggledOnce: Flow<Boolean> =
        context.dataStore.data.map { it[KEY_MONITOR_TOGGLED_ONCE] ?: false }

    suspend fun isMonitorEnabledNow(): Boolean = monitorEnabled.first()
    suspend fun isOnboardingDoneNow(): Boolean = onboardingDone.first()
    suspend fun wasMonitorToggledOnce(): Boolean = monitorToggledOnce.first()

    suspend fun setDefaultLimit(value: Int) {
        context.dataStore.edit { it[KEY_DEFAULT_LIMIT] = value }
    }

    suspend fun setMonitorEnabled(value: Boolean) {
        context.dataStore.edit { it[KEY_MONITOR_ON] = value }
    }

    suspend fun setMonitorToggledOnce(value: Boolean) {
        context.dataStore.edit { it[KEY_MONITOR_TOGGLED_ONCE] = value }
    }

    suspend fun setOnboardingDone(value: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_DONE] = value }
    }

    suspend fun setEscalationOn(value: Boolean) {
        context.dataStore.edit { it[KEY_ESCALATION] = value }
    }

    suspend fun setCombineAppsTime(value: Boolean) {
        context.dataStore.edit { it[KEY_COMBINE_APPS] = value }
    }

    suspend fun setSessionAlertsOn(value: Boolean) { context.dataStore.edit { it[KEY_SESSION_ALERTS] = value } }
    suspend fun setDailyWarningOn(value: Boolean) { context.dataStore.edit { it[KEY_DAILY_WARNING] = value } }
    suspend fun setBlockingOn(value: Boolean) { context.dataStore.edit { it[KEY_BLOCKING] = value } }
    suspend fun setBlockAtSessionLimit(value: Boolean) { context.dataStore.edit { it[KEY_BLOCK_AT_SESSION] = value } }
    suspend fun setReentryOn(value: Boolean) { context.dataStore.edit { it[KEY_REENTRY] = value } }
    suspend fun setDailyOverlayMinutes(value: Int) { context.dataStore.edit { it[KEY_DAILY_LIMIT] = value } }
    suspend fun setWorkAlertsOn(value: Boolean) { context.dataStore.edit { it[KEY_WORK_ALERTS] = value } }
}
