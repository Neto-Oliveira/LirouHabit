package com.lirouhabit.app.monitor

import java.text.SimpleDateFormat
import java.util.ArrayDeque
import java.util.Date
import java.util.Locale

/**
 * Log de diagnóstico em memória (últimas 100 linhas).
 *
 * Serve para a tela secreta de desenvolvimento (3 chacoalhadas) mostrar
 * exatamente o que o monitor está vendo: app em foreground, pausas, marcos
 * disparados e falhas ao postar notificação.
 */
object MonitorLog {
    private const val MAX_LINES = 100
    private val lines = ArrayDeque<String>()
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    @Synchronized
    fun add(message: String) {
        lines.addLast("${timeFormat.format(Date())}  $message")
        while (lines.size > MAX_LINES) lines.removeFirst()
    }

    @Synchronized
    fun recent(): List<String> = lines.toList()

    @Synchronized
    fun clear() {
        lines.clear()
    }
}
