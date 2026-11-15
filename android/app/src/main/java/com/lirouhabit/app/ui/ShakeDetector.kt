package com.lirouhabit.app.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import kotlin.math.abs

/**
 * Modo secreto: 3 chacoalhadas fortes em sequência abrem a tela de
 * desenvolvimento (modo de teste). Sem botão visível em lugar nenhum.
 */
@Composable
fun ShakeToDevMode(onShake: () -> Unit) {
    val context = LocalContext.current
    val callback by rememberUpdatedState(onShake)

    DisposableEffect(Unit) {
        val manager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = manager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        var lastX = 0f
        var lastY = 0f
        var lastZ = 0f
        var lastEventAt = 0L
        var shakes = 0
        var lastShakeAt = 0L

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val now = System.currentTimeMillis()
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                // Primeira leitura (ou leitura muito espaçada): só guarda a base.
                if (now - lastEventAt > 500) {
                    lastX = x; lastY = y; lastZ = z
                    lastEventAt = now
                    return
                }

                val delta = abs(x - lastX) + abs(y - lastY) + abs(z - lastZ)
                lastX = x; lastY = y; lastZ = z
                lastEventAt = now

                if (delta < 18f) return // limiar alto: só chacoalhada de verdade

                shakes = if (now - lastShakeAt in 0..1500) shakes + 1 else 1
                lastShakeAt = now
                if (shakes >= 3) {
                    shakes = 0
                    callback()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        manager?.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        onDispose { manager?.unregisterListener(listener) }
    }
}
