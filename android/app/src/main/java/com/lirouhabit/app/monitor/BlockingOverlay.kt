package com.lirouhabit.app.monitor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Tela de bloqueio exibida POR CIMA do app vilão — só no marco de 1 hora.
 * Sem clichês: mostra o tempo real gasto, uma frase que toca na ferida
 * e o sonho do usuário. Sair é fácil; continuar exige 15s de espera.
 */
@Composable
fun BlockingOverlay(
    phrase: String,
    appName: String,
    dream: String,
    elapsedMinutes: Int,
    onVictory: () -> Unit,
    onDefeat: () -> Unit
) {
    var secondsLeft by remember { mutableIntStateOf(15) }
    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1_000)
            secondsLeft--
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xF20A0A0A)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // O número que dói: tempo real gasto
            Text(
                "$elapsedMinutes",
                color = Color(0xFFE53935),
                fontSize = 84.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                "minutos no $appName",
                color = Color(0xFFB0B0B0),
                fontSize = 16.sp
            )

            Spacer(Modifier.height(28.dp))
            Text(
                "\"$phrase\"",
                color = Color.White,
                fontSize = 19.sp,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center
            )

            if (dream.isNotBlank()) {
                Spacer(Modifier.height(28.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Color(0x33E53935))) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "🏆 SEU SONHO",
                            color = Color(0xFFFFB300),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            dream,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            Text(
                "Valeu a pena?",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onVictory,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("✅ VOLTAR AO QUE IMPORTA", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onDefeat,
                enabled = secondsLeft == 0,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFE57373),
                    disabledContentColor = Color(0xFF757575)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    if (secondsLeft > 0) "Aguarde ${secondsLeft}s…"
                    else "Continuar mesmo assim"
                )
            }
        }
    }
}
