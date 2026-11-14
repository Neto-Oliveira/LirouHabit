package com.lirouhabit.app.monitor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lirouhabit.app.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Religa o monitor automaticamente após reiniciar o celular (se estava ativado). */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (SettingsRepository(context).isMonitorEnabledNow()) {
                    MonitorService.start(context)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
