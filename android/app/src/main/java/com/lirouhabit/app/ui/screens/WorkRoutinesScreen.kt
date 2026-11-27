package com.lirouhabit.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lirouhabit.app.R
import com.lirouhabit.app.data.WorkRoutine
import com.lirouhabit.app.ui.IconLabel
import com.lirouhabit.app.ui.vm.AppViewModel

/** Cadastro de horários de trabalho/foco. O monitor faz o resto. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkRoutinesScreen(vm: AppViewModel, onBack: () -> Unit) {
    val routines by vm.workRoutines.collectAsState()
    val workAlertsOn by vm.workAlertsOn.collectAsState()
    var editing by remember { mutableStateOf<WorkRoutine?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { IconLabel(iconRes = R.drawable.ic_target, text = "Tempo de trabalho") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editing = null; showEditor = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar rotina")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Alertas durante o trabalho", fontWeight = FontWeight.Bold)
                            Text(
                                "A cada 1 minuto em app vilão: \"Ei, você não devia estar aqui — hora do trabalho\".",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = workAlertsOn, onCheckedChange = { vm.setWorkAlertsOn(it) })
                    }
                }
            }
            if (routines.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp)) {
                        Text(
                            "Cadastre sua jornada.\nEx.: Trabalho · Seg–Sex · 08:00–18:00",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            items(routines, key = { it.id }) { routine ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f).clickable {
                                editing = routine
                                showEditor = true
                            }
                        ) {
                            Text(routine.label, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Text(
                                "${formatDays(routine.daysMask)} · ${formatMinute(routine.startMinute)}–${formatMinute(routine.endMinute)}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = routine.enabled,
                            onCheckedChange = { vm.setWorkRoutineEnabled(routine, it) }
                        )
                        IconButton(onClick = { vm.deleteWorkRoutine(routine) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        WorkRoutineEditor(
            initial = editing,
            onDismiss = { showEditor = false },
            onSave = {
                vm.saveWorkRoutine(it)
                showEditor = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkRoutineEditor(
    initial: WorkRoutine?,
    onDismiss: () -> Unit,
    onSave: (WorkRoutine) -> Unit
) {
    var label by remember { mutableStateOf(initial?.label ?: "Trabalho") }
    var start by remember { mutableStateOf(formatMinute(initial?.startMinute ?: 8 * 60)) }
    var end by remember { mutableStateOf(formatMinute(initial?.endMinute ?: 18 * 60)) }
    var daysMask by remember { mutableIntStateOf(initial?.daysMask ?: 31) }
    var error by remember { mutableStateOf("") }
    val dayNames = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Nova rotina" else "Editar rotina") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Nome (ex.: Trabalho)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = start,
                        onValueChange = { start = it },
                        label = { Text("Início") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = end,
                        onValueChange = { end = it },
                        label = { Text("Fim") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Text("Dias", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    dayNames.take(4).forEachIndexed { i, name ->
                        DayChip(name, i, daysMask) { daysMask = it }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    dayNames.drop(4).forEachIndexed { i, name ->
                        DayChip(name, i + 4, daysMask) { daysMask = it }
                    }
                }
                if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                Text("Horário 24h (HH:mm). Pode atravessar a meia-noite.", fontSize = 11.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val startMin = parseMinute(start)
                val endMin = parseMinute(end)
                when {
                    label.isBlank() -> error = "Dê um nome para a rotina."
                    startMin == null || endMin == null -> error = "Use horários válidos no formato HH:mm."
                    daysMask == 0 -> error = "Escolha pelo menos um dia."
                    startMin == endMin -> error = "Início e fim não podem ser iguais."
                    else -> onSave(
                        WorkRoutine(
                            id = initial?.id ?: 0,
                            label = label.trim(),
                            daysMask = daysMask,
                            startMinute = startMin,
                            endMinute = endMin,
                            enabled = initial?.enabled ?: true,
                            createdAt = initial?.createdAt ?: System.currentTimeMillis()
                        )
                    )
                }
            }) { Text("SALVAR") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayChip(label: String, index: Int, mask: Int, onChange: (Int) -> Unit) {
    val selected = mask and (1 shl index) != 0
    FilterChip(
        selected = selected,
        onClick = { onChange(mask xor (1 shl index)) },
        label = { Text(label, fontSize = 11.sp) }
    )
}

private fun parseMinute(value: String): Int? {
    val match = Regex("^(\\d{1,2}):(\\d{2})$").find(value.trim()) ?: return null
    val hour = match.groupValues[1].toInt()
    val minute = match.groupValues[2].toInt()
    return if (hour in 0..23 && minute in 0..59) hour * 60 + minute else null
}

private fun formatMinute(value: Int): String = "%02d:%02d".format(value / 60, value % 60)

private fun formatDays(mask: Int): String = when (mask) {
    31 -> "Seg–Sex"
    127 -> "Todos os dias"
    else -> listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")
        .filterIndexed { i, _ -> mask and (1 shl i) != 0 }
        .joinToString(", ")
}
