package com.lirouhabit.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lirouhabit.app.R
import com.lirouhabit.app.ui.BrandAmber
import com.lirouhabit.app.ui.IconLabel
import com.lirouhabit.app.ui.vm.AppViewModel

/** Passo 2 da introdução: captura do maior sonho do usuário (a "munição" do app). */
@Composable
fun IntroDreamScreen(vm: AppViewModel, onNext: () -> Unit) {
    var text by remember { mutableStateOf("") }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(28.dp),
            verticalArrangement = Arrangement.Center
        ) {
            IconLabel(
                iconRes = R.drawable.ic_trophy,
                text = "Seu maior sonho",
                iconTint = BrandAmber,
                iconSize = 28,
                fontSize = 26
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Se você pudesse realizar UMA coisa nos próximos anos, qual seria?",
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 26.sp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Escreva com verdade. É isso que o LirouHabit vai colocar na sua cara " +
                    "toda vez que você fugir para a procrastinação.",
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(28.dp))
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Ex.: abrir meu negócio, passar no concurso…") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    vm.saveDream(id = 0, text = text, priority = 3)
                    onNext()
                },
                enabled = text.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("CONTINUAR", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
