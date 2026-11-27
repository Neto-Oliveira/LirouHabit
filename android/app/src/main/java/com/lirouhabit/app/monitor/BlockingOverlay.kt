package com.lirouhabit.app.monitor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lirouhabit.app.R
import kotlinx.coroutines.delay

/**
 * Intervenção full-screen por cima do vilão.
 * Sair para o que importa é imediato; continuar exige 15s de fricção.
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

    val timeLabel = if (elapsedMinutes >= 60) {
        val h = elapsedMinutes / 60
        val m = elapsedMinutes % 60
        if (m == 0) "${h}h" else "${h}h ${m}min"
    } else {
        "$elapsedMinutes min"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF260707), Color(0xFF0C0C0C), Color(0xFF050505))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(color = Color(0x33E53935), shape = RoundedCornerShape(50)) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_siren),
                        contentDescription = null,
                        tint = Color(0xFFFF6B60),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "PAUSA OBRIGATÓRIA",
                        color = Color(0xFFFF8A80),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp
                    )
                }
            }

            Spacer(Modifier.height(22.dp))
            Text(
                "VOCÊ JÁ USOU",
                color = Color(0xFF8F8F8F),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                timeLabel,
                color = Color(0xFFFF4D42),
                fontSize = 64.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                lineHeight = 68.sp
            )
            Text(
                "em $appName hoje",
                color = Color(0xFFB5B5B5),
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(24.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1111)),
                border = BorderStroke(1.dp, Color(0x55E53935)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    phrase,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 28.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(20.dp)
                )
            }

            if (dream.isNotBlank()) {
                Spacer(Modifier.height(14.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF171717)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(R.drawable.ic_trophy),
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(7.dp))
                            Text(
                                "O QUE ESTÁ ESPERANDO POR VOCÊ",
                                color = Color(0xFFFFB300),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(Modifier.height(7.dp))
                        Text(
                            dream,
                            color = Color.White,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "Agora não é impulso. É escolha.",
                color = Color(0xFFD0D0D0),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(14.dp))

            Button(
                onClick = onVictory,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E9E47)),
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_check_circle),
                    contentDescription = null,
                    modifier = Modifier.size(21.dp)
                )
                Spacer(Modifier.width(9.dp))
                Text("VOLTAR AO QUE IMPORTA", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onDefeat,
                enabled = secondsLeft == 0,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFFF8A80),
                    disabledContentColor = Color(0xFF666666)
                ),
                border = BorderStroke(1.dp, if (secondsLeft == 0) Color(0x88E53935) else Color(0xFF333333)),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(if (secondsLeft > 0) "Decida sem impulso: ${secondsLeft}s" else "Continuar mesmo assim")
            }

            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { (15 - secondsLeft) / 15f },
                modifier = Modifier.fillMaxWidth().height(3.dp),
                color = Color(0xFFE53935),
                trackColor = Color(0xFF2A2A2A)
            )
        }
    }
}
