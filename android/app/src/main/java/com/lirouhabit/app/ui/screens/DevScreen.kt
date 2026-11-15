package com.lirouhabit.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lirouhabit.app.R
import com.lirouhabit.app.monitor.MonitorLog
import com.lirouhabit.app.monitor.MonitorService
import com.lirouhabit.app.ui.BrandRed
import com.lirouhabit.app.ui.IconLabel
import com.lirouhabit.app.ui.VictoryGreen
import com.lirouhabit.app.ui.vm.AppViewModel
import kotlinx.coroutines.delay

/**
 * Tela oculta de desenvolvimento / modo de teste.
 *
 * Não existe botão para chegar aqui: é preciso chacoalhar o celular 3 vezes
 * (ver [com.lirouhabit.app.ui.ShakeToDevMode]). Mostra o estado real das
 * permissões, do serviço e do loop, permite disparar intervenções na hora e
 * exibe o log ao vivo do monitor.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevScreen(vm: AppViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val monitorOn by vm.monitorEnabled.collectAsState()

    var version by remember { mutableStateOf("?") }
    var detected by remember { mutableStateOf("…") }
    var sessionText by remember { mutableStateOf("nenhuma") }
    var serviceRunning by remember { mutableStateOf(MonitorService.isRunning) }
    var logLines by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(Unit) {
        version = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "?"
        vm.refreshPermissions()
        while (true) {
            detected = vm.detectForegroundNow()
            serviceRunning = MonitorService.isRunning
            sessionText = MonitorService.sessionApp?.let { appName ->
                val minutes = MonitorService.sessionMs / 60_000.0
                "$appName — %.1f min".format(minutes)
            } ?: "nenhuma"
            logLines = MonitorLog.recent().reversed()
            delay(1_500)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { IconLabel(iconRes = R.drawable.ic_bolt, text = "Modo de teste") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                "Tela oculta — 3 chacoalhadas abrem aqui.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            // ---------- Estado real ----------
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Estado", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    InfoLine("Versão", version, ok = true)
                    InfoLine(
                        "Serviço do monitor",
                        if (serviceRunning) "ATIVO" else "PARADO",
                        ok = serviceRunning
                    )
                    InfoLine("Monitor ligado", if (monitorOn) "sim" else "não", ok = monitorOn)
                    InfoLine(
                        "Acesso ao uso",
                        if (vm.permUsage) "concedido" else "FALTANDO",
                        ok = vm.permUsage
                    )
                    InfoLine(
                        "Sobrepor outros apps",
                        if (vm.permOverlay) "concedido" else "FALTANDO",
                        ok = vm.permOverlay
                    )
                    InfoLine(
                        "Notificações",
                        if (vm.permNotif) "concedido" else "FALTANDO",
                        ok = vm.permNotif
                    )
                    InfoLine(
                        "Bateria sem restrição",
                        if (vm.permBattery) "sim" else "não",
                        ok = vm.permBattery
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Detectando agora: $detected",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Sessão atual: $sessionText",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandRed
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ---------- Testes de ponta a ponta ----------
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Testar intervenção", fontWeight = FontWeight.Bold)
                    Text(
                        "Dispara na hora. Se nada aparecer, a permissão correspondente está faltando.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { vm.testAlert(level = 1, minutes = 5) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Alerta 5 min (leve)") }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { vm.testAlert(level = 2, minutes = 15) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Alerta 15 min (pesado)") }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { vm.testOverlay() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Tela de bloqueio (overlay 1h)") }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { vm.forceMilestone() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Forçar marco (via serviço) — testa ponta a ponta") }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ---------- Log ao vivo ----------
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Log do monitor",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = {
                                MonitorLog.clear()
                                logLines = emptyList()
                            }
                        ) { Text("limpar") }
                    }
                    if (logLines.isEmpty()) {
                        Text(
                            "vazio — abra o Instagram por alguns segundos e volte.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        logLines.forEach { line ->
                            Text(
                                line,
                                fontSize = 10.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String, ok: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Text("$label: ", fontSize = 12.5.sp)
        Text(
            value,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (ok) VictoryGreen else BrandRed
        )
    }
}
