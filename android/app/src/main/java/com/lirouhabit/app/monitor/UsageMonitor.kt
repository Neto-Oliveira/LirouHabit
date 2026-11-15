package com.lirouhabit.app.monitor

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.os.Process

class UsageMonitor(private val context: Context) {

    /**
     * Retorna o package do app em foreground.
     * 1) Eventos de foreground (preciso) — janela de 10s.
     * 2) Fallback: app usado mais recentemente (ROMs que atrasam eventos, ex.: MIUI).
     */
    fun currentForegroundApp(): String? {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()

        // 1) Eventos
        val events = usm.queryEvents(end - 10_000, end)
        val event = UsageEvents.Event()
        var foreground: String? = null
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND ||
                event.eventType == UsageEvents.Event.ACTIVITY_RESUMED
            ) {
                foreground = event.packageName
            }
        }
        if (foreground != null) return foreground

        // 2) Fallback por lastTimeUsed
        val stats = usm.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            end - 120_000,
            end
        )
        return stats
            ?.filter { it.lastTimeUsed > 0 }
            ?.maxByOrNull { it.lastTimeUsed }
            ?.packageName
    }

    companion object {
        /** Checa se o usuário concedeu "Acesso ao uso" (compatível com Android 8+). */
        fun hasUsageAccess(context: Context): Boolean {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            }
            return mode == AppOpsManager.MODE_ALLOWED
        }
    }
}
