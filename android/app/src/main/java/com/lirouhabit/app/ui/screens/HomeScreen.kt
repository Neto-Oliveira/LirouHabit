package com.lirouhabit.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lirouhabit.app.R
import com.lirouhabit.app.ui.BrandAmber
import com.lirouhabit.app.ui.BrandRed
import com.lirouhabit.app.ui.NavCard
import com.lirouhabit.app.ui.Routes
import com.lirouhabit.app.ui.StatCard
import com.lirouhabit.app.ui.VictoryGreen
import com.lirouhabit.app.ui.vm.AppViewModel

@Composable
fun HomeScreen(vm: AppViewModel, nav: NavController) {
    val monitorOn by vm.monitorEnabled.collectAsState()
    val triggers by vm.triggers.collectAsState()
    val victories by vm.victories.collectAsState()
    val defeats by vm.defeats.collectAsState()
    val tracked by vm.trackedApps.collectAsState()

    LaunchedEffect(Unit) { vm.refreshPermissions() }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_flame),
                    contentDescription = null,
                    tint = BrandRed,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text("LirouHabit", fontSize = 30.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                "Pare de trocar seus sonhos por scroll.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(20.dp))

            if (!vm.corePermissionsGranted) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { nav.navigate(Routes.ONBOARDING) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4A2C0A))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_alert),
                            contentDescription = null,
                            tint = Color(0xFFFFCC80),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Permissões pendentes — o monitor não funciona sem elas. Toque para revisar.",
                            color = Color(0xFFFFCC80),
                            fontSize = 13.sp
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            if (vm.corePermissionsGranted && !monitorOn) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { vm.setMonitor(true) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4A0A0A))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_siren),
                            contentDescription = null,
                            tint = Color(0xFFEF9A9A),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Monitor DESLIGADO — sem ele, nada te confronta. Toque aqui para ligar.",
                            color = Color(0xFFEF9A9A),
                            fontSize = 13.sp
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (monitorOn) Color(0xFF1B3A1E)
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (monitorOn) "Monitor ATIVO" else "Monitor DESLIGADO",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            if (monitorOn) {
                                "Vigiando ${tracked.count { it.enabled }} app(s) vilão(ões)"
                            } else {
                                "Ligue para começar a ser confrontado"
                            },
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = monitorOn,
                        onCheckedChange = { vm.setMonitor(it) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(R.drawable.ic_siren, triggers, "Intervenções", Modifier.weight(1f), iconTint = BrandAmber)
                StatCard(R.drawable.ic_check_circle, victories, "Vitórias", Modifier.weight(1f), iconTint = VictoryGreen)
                StatCard(R.drawable.ic_x_circle, defeats, "Derrotas", Modifier.weight(1f), iconTint = BrandRed)
            }

            Spacer(Modifier.height(20.dp))
            NavCard(R.drawable.ic_trophy, "Meus Sonhos", "O que você quer conquistar") {
                nav.navigate(Routes.DREAMS)
            }
            NavCard(R.drawable.ic_chain, "Vícios & Maus Hábitos", "O que você quer largar") {
                nav.navigate(Routes.VICES)
            }
            NavCard(R.drawable.ic_phone, "Apps Vilões", "Quem está roubando seu tempo") {
                nav.navigate(Routes.APPS)
            }
            NavCard(R.drawable.ic_chart, "Estatísticas", "Seu histórico de confronto") {
                nav.navigate(Routes.STATS)
            }
            NavCard(R.drawable.ic_chart, "Tempo por dia (tracker)", "Evolução do tempo em apps vilões") {
                nav.navigate(Routes.TRACKER)
            }
            NavCard(R.drawable.ic_gear, "Configurações", "Limites, agressividade e permissões") {
                nav.navigate(Routes.SETTINGS)
            }
        }
    }
}
