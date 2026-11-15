package com.lirouhabit.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lirouhabit.app.R
import com.lirouhabit.app.ui.BrandRed
import com.lirouhabit.app.ui.IconLabel
import com.lirouhabit.app.ui.vm.AppViewModel
import com.lirouhabit.app.ui.vm.InstalledApp

/** Passo 3 da introdução: seleção múltipla dos apps vilões que roubam o foco. */
@Composable
fun IntroAppsScreen(vm: AppViewModel, onNext: () -> Unit) {
    var apps by remember { mutableStateOf<List<InstalledApp>?>(null) }
    var selected by remember { mutableStateOf(setOf<String>()) }
    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { apps = vm.loadInstalledApps() }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            IconLabel(
                iconRes = R.drawable.ic_phone,
                text = "Seus apps vilões",
                iconTint = BrandRed,
                iconSize = 28,
                fontSize = 26
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Quais aplicativos roubam o seu foco hoje?",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Seja honesto — são eles que o monitor vai vigiar. Limites e outros apps ficam configuráveis depois.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Buscar app (ex.: Instagram)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            val list = apps
            if (list == null) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            } else {
                val filtered = list.filter {
                    it.name.contains(query, ignoreCase = true)
                }
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filtered, key = { it.packageName }) { app ->
                        val isSelected = app.packageName in selected
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    selected =
                                        if (isSelected) selected - app.packageName
                                        else selected + app.packageName
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0x33E53935)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (app.icon != null) {
                                    Image(
                                        bitmap = app.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp)
                                    )
                                } else {
                                    Spacer(Modifier.size(36.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    app.name,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 15.sp
                                )
                                if (isSelected) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_check_circle),
                                        contentDescription = "Selecionado",
                                        tint = BrandRed,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    apps
                        ?.filter { it.packageName in selected }
                        ?.forEach { vm.addTrackedApp(it.packageName, it.name) }
                    onNext()
                },
                enabled = selected.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    if (selected.isEmpty()) "SELECIONE AO MENOS 1 APP"
                    else "CONTINUAR (${selected.size} SELECIONADOS)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
