package com.lirouhabit.app.monitor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lirouhabit.app.App
import com.lirouhabit.app.MainActivity
import com.lirouhabit.app.data.InterventionEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Recebe as ações da notificação de intervenção:
 *  - "Fechar o app" → vitória (contabiliza VICTORY, manda pro home)
 *  - "Ignorar" ou dispensar (swipe) → derrota (contabiliza DEFEAT, snooze 5 min)
 */
class InterventionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as? App ?: return@launch
                val db = app.db
                val pkg = intent.getStringExtra(EXTRA_PKG) ?: return@launch
                val appName = intent.getStringExtra(EXTRA_APP) ?: pkg
                val phrase = intent.getStringExtra(EXTRA_PHRASE) ?: ""
                val minutes = intent.getIntExtra(EXTRA_MINUTES, 0)

                val victory = intent.action == ACTION_CLOSE_APP

                db.eventDao().insert(
                    InterventionEvent(
                        packageName = pkg,
                        appName = appName,
                        outcome = if (victory) "VICTORY" else "DEFEAT",
                        phrase = phrase
                    )
                )

                if (victory) {
                    MonitorLog.add("VITÓRIA: usuário fechou $appName")
                    val home = Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_HOME)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    runCatching { context.startActivity(home) }
                } else {
                    MonitorLog.add("DERROTA: usuário ignorou $appName")
                    MonitorService.snooze(pkg, 5 * 60_000L)
                    MonitorService.resetMilestone(pkg, minutes)
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_CLOSE_APP = "com.lirouhabit.app.CLOSE_APP"
        const val ACTION_IGNORE = "com.lirouhabit.app.IGNORE"
        const val ACTION_DISMISS = "com.lirouhabit.app.DISMISS"

        const val EXTRA_PKG = "pkg"
        const val EXTRA_APP = "app"
        const val EXTRA_PHRASE = "phrase"
        const val EXTRA_MINUTES = "minutes"
    }
}
