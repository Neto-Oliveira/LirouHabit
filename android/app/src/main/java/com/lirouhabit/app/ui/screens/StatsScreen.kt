package com.lirouhabit.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lirouhabit.app.R
import com.lirouhabit.app.ui.BrandAmber
import com.lirouhabit.app.ui.BrandRed
import com.lirouhabit.app.ui.IconLabel
import com.lirouhabit.app.ui.StatCard
import com.lirouhabit.app.ui.VictoryGreen
import com.lirouhabit.app.ui.vm.AppViewModel
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(vm: AppViewModel, onBack: () -> Unit) {
    val events by vm.events.collectAsState()
    val triggers by vm.triggers.collectAsState()
    val victories by vm.victories.collectAsState()
    val defeats by vm.defeats.collectAsState()
    val usage by vm.dailyUsage.collectAsState()
    val fmt = remember { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()) }

    // ---- Tempo de uso: últimos 7 dias + semanas (semana = seg→dom, zera na segunda) ----
    val today = LocalDate.now()
    val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val lastWeekStart = weekStart.minusWeeks(1)

    fun msOn(date: LocalDate): Long {
        val iso = date.toString() // yyyy-MM-dd
        return usage.filter { it.date == iso }.sumOf { it.totalMs }
    }

    val last7 = (6 downTo 0).map { offset ->
        val date = today.minusDays(offset.toLong())
        date to msOn(date) / 60_000.0
    }
    val thisWeekMs = (0..6).sumOf { msOn(weekStart.plusDays(it.toLong())) }
    val lastWeekMs = (0..6).sumOf { msOn(lastWeekStart.plusDays(it.toLong())) }
    val diffMs = thisWeekMs - lastWeekMs

    Scaffold(
        topBar = {
            TopAppBar(
                title = { IconLabel(iconRes = R.drawable.ic_chart, text = "Estatísticas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    StatCard(R.drawable.ic_siren, triggers, "Intervenções", Modifier.weight(1f), iconTint = BrandAmber)
                    StatCard(R.drawable.ic_check_circle, victories, "Vitórias", Modifier.weight(1f), iconTint = VictoryGreen)
                    StatCard(R.drawable.ic_x_circle, defeats, "Derrotas", Modifier.weight(1f), iconTint = BrandRed)
                }
                Spacer(Modifier.height(20.dp))

                // ---- Tempo de uso nos apps vilões ----
                Text("Tempo nos apps vilões", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        UsageBarChart(last7)
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "em minutos por dia (inclui hoje, ainda contando)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Visão semanal (zera na segunda)", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Esta semana", modifier = Modifier.weight(1f), fontSize = 13.sp)
                            Text(
                                "%.0f min".format(thisWeekMs / 60_000.0),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Semana passada", modifier = Modifier.weight(1f), fontSize = 13.sp)
                            Text(
                                "%.0f min".format(lastWeekMs / 60_000.0),
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        val (txt, tint) = when {
                            lastWeekMs == 0L -> "Sem base de comparação ainda." to MaterialTheme.colorScheme.onSurfaceVariant
                            diffMs < 0 -> "-%.0f min vs. semana passada. Evoluindo!".format(-diffMs / 60_000.0) to VictoryGreen
                            diffMs == 0L -> "Igual à semana passada. Dá pra cortar mais." to BrandAmber
                            else -> "+%.0f min vs. semana passada. Hora de se confrontar.".format(diffMs / 60_000.0) to BrandRed
                        }
                        Text(txt, fontSize = 12.sp, color = tint)
                    }
                }

                Spacer(Modifier.height(20.dp))
                Text("Últimos confrontos", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
            }

            if (events.isEmpty()) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_eye),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Nenhum confronto ainda. Ou você está focado… ou o monitor está desligado.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            items(events, key = { it.id }) { ev ->
                val (iconRes, tint, label) = when (ev.outcome) {
                    "VICTORY" -> Triple(R.drawable.ic_check_circle, VictoryGreen, "Vitória — voltou ao foco")
                    "DEFEAT" -> Triple(R.drawable.ic_x_circle, BrandRed, "Derrota — continuou no app")
                    else -> Triple(R.drawable.ic_siren, BrandAmber, "Intervenção disparada")
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "${ev.appName} — $label",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (ev.phrase.isNotBlank()) {
                                Text(
                                    "\"${ev.phrase}\"",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                fmt.format(Date(ev.timestamp)),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Barras de tempo de uso por dia (últimos 7 dias, do mais antigo ao de hoje).
 * Sem biblioteca externa: só Row + Box, então o APK continua leve.
 */
@Composable
private fun UsageBarChart(days: List<Pair<LocalDate, Double>>) {
    val max = days.maxOfOrNull { it.second } ?: 0.0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        days.forEach { (date, minutes) ->
            val fraction = if (max > 0) (minutes / max).toFloat() else 0f
            val label = date.dayOfWeek
                .getDisplayName(TextStyle.SHORT, Locale("pt", "BR"))
                .lowercase()
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((fraction.coerceAtLeast(0.03f) * 110).dp)
                            .background(
                                if (minutes > 0) BrandRed else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(6.dp)
                            )
                    )
                }
                Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("%.0f".format(minutes), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
