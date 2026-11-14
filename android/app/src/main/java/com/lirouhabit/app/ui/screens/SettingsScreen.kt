package com.lirouhabit.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lirouhabit.app.ui.Routes
import com.lirouhabit.app.ui.vm.AppViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: AppViewModel, nav: NavController, onBack: () -> Unit) {
    val context = LocalContext.current
    val defaultLimit by vm.defaultLimit.collectAsState()
    val escalation by vm.escalationOn.collectAsState()

    LaunchedEffect(Unit) { vm.refreshPermissions() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚙️ Configurações") },
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
            // Limite padrão
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tempo limite padrão", fontWeight = FontWeight.Bold)
                    Text(
                        "$defaultLimit min — aplicado a apps vilões novos",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = defaultLimit.toFloat(),
                        onValueChange = { vm.setDefaultLimit(it.roundToInt().coerceIn(1, 60)) },
                        valueRange = 1f..60f,
                        steps = 58
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Escalonamento
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Agressividade crescente 🔥", fontWeight = FontWeight.Bold)
                        Text(
                            "Cada reincidência no mesmo dia deixa a mensagem mais pesada",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = escalation, onCheckedChange = { vm.setEscalation(it) })
                }
            }

            Spacer(Modifier.height(12.dp))

            // Permissões
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Permissões", fontWeight = FontWeight.Bold)
                    Text(
                        "Uso: ${if (vm.permUsage) "✅" else "❌"}  ·  Overlay: ${if (vm.permOverlay) "✅" else "❌"}  ·  " +
                            "Notificações: ${if (vm.permNotif) "✅" else "❌"}  ·  Bateria: ${if (vm.permBattery) "✅" else "❌"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { nav.navigate(Routes.ONBOARDING) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Revisar permissões") }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            runCatching {
                                context.startActivity(
                                    Intent(
                                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Bateria sem restrição") }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Sobre
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Sobre", fontWeight = FontWeight.Bold)
                    Text(
                        "LirouHabit v1.0.0 · com.lirouhabit.app\n" +
                            "100% offline: seus sonhos e vícios ficam só no seu celular.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
