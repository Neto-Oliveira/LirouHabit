package com.lirouhabit.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import com.lirouhabit.app.data.AppDatabase
import com.lirouhabit.app.data.SettingsRepository
import com.lirouhabit.app.monitor.MonitorService

class App : Application() {

    lateinit var db: AppDatabase
        private set
    lateinit var settings: SettingsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        db = AppDatabase.get(this)
        settings = SettingsRepository(this)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel(
                    MonitorService.CHANNEL_STATUS,
                    "Monitor de foco",
                    NotificationManager.IMPORTANCE_MIN
                )
            )
            nm.createNotificationChannel(
                NotificationChannel(
                    MonitorService.CHANNEL_ALERT,
                    "Alertas anti-procrastinação",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Intervenções quando você passa do limite nos apps vilões"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 150, 500, 150, 800)
                    // Som característico do app (tom de alerta próprio, stream de alarme)
                    setSound(
                        Uri.parse("android.resource://$packageName/${R.raw.alert_tone}"),
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                }
            )
        }
    }
}

val Context.lirouApp: App
    get() = applicationContext as App
