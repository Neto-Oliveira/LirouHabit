package com.lirouhabit.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(vm: AppViewModel, onBack: () -> Unit) {
    val events by vm.events.collectAsState()
    val triggers by vm.triggers.collectAsState()
    val victories by vm.victories.collectAsState()
    val defeats by vm.defeats.collectAsState()
    val fmt = remember { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()) }

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
                Spacer(Modifier.height(16.dp))
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
