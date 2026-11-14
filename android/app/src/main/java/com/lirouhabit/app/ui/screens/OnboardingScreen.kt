package com.lirouhabit.app.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lirouhabit.app.ui.vm.AppViewModel
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(vm: AppViewModel, onDone: () -> Unit) {
    val context = LocalContext.current
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { vm.refreshPermissions() }

    // Atualiza o status das permissões enquanto a tela estiver aberta
    LaunchedEffect(Unit) {
        vm.refreshPermissions()
        while (true) {
            delay(1_500)
            vm.refreshPermissions()
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text("Quase lá 💪", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Para te confrontar na hora certa, o LirouHabit precisa de 4 permissões. " +
                    "Sem as 3 primeiras, o monitor simplesmente não funciona.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(20.dp))

            PermissionCard(
                title = "1. Acesso ao uso",
                desc = "Detecta qual app está aberto (Instagram, TikTok…)",
                granted = vm.permUsage,
                onGrant = {
                    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }
            )
            PermissionCard(
                title = "2. Sobrepor outros apps",
                desc = "Mostra a tela de bloqueio por cima do app vilão",
                granted = vm.permOverlay,
                onGrant = {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                    )
                }
            )
            PermissionCard(
                title = "3. Notificações",
                desc = "Dispara o alerta agressivo quando o tempo estoura",
                granted = vm.permNotif,
                onGrant = {
                    if (Build.VERSION.SDK_INT >= 33) {
                        notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            )
            PermissionCard(
                title = "4. Bateria sem restrição",
                desc = "Impede o Android (Xiaomi, Samsung…) de matar o monitor em 2º plano",
                granted = vm.permBattery,
                onGrant = {
                    runCatching {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                Uri.parse("package:${context.packageName}")
                            )
                        )
                    }
                }
            )

            Spacer(Modifier.height(28.dp))
            Button(
                onClick = { vm.finishOnboarding(); onDone() },
                enabled = vm.corePermissionsGranted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text("COMEÇAR — FOCO TOTAL 🔥", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            if (!vm.corePermissionsGranted) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Conceda ao menos as 3 primeiras permissões para continuar",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    desc: String,
    granted: Boolean,
    onGrant: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (granted) Color(0xFF1B3A1E)
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(
                    desc,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (granted) {
                Text("✅", fontSize = 22.sp)
            } else {
                TextButton(onClick = onGrant) { Text("PERMITIR") }
            }
        }
    }
}
