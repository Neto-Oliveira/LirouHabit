package com.lirouhabit.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lirouhabit.app.R
import com.lirouhabit.app.ui.BrandRed

/** Primeira tela do app: apresentação de impacto. Sem botão de pular — a verdade dói. */
@Composable
fun IntroScreen(onNext: () -> Unit) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(28.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_flame),
                contentDescription = null,
                tint = BrandRed,
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(28.dp))
            Text(
                "Quantos sonhos você já deixou de realizar por causa da procrastinação?",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 34.sp
            )
            Spacer(Modifier.height(20.dp))
            Text(
                "Não foi por falta de talento. Nem de tempo.\n\n" +
                    "Foi um scroll de cada vez. 5 minutos aqui, 15 ali — e, quando você viu, " +
                    "o dia virou mês. O mês virou ano.",
                fontSize = 15.sp,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Este app existe para acabar com isso. De forma agressiva.",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = BrandRed
            )
            Spacer(Modifier.height(44.dp))
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("ENCARAR A VERDADE", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
