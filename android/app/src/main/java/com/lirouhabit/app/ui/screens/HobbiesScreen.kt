package com.lirouhabit.app.ui.screens

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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.lirouhabit.app.data.Hobby
import com.lirouhabit.app.monitor.PhraseBank
import com.lirouhabit.app.ui.IconLabel
import com.lirouhabit.app.ui.vm.AppViewModel

/** Hobbies: o que o usuário quer colocar no lugar do scroll infinito. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HobbiesScreen(vm: AppViewModel, onBack: () -> Unit) {
    val hobbies by vm.hobbies.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Hobby?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { IconLabel(iconRes = R.drawable.ic_star, text = "Meus Hobbies") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editing = null; showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar hobby")
            }
        }
    ) { padding ->
        if (hobbies.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "O que você faria se o celular sumisse por 1 hora?\nEx.: tocar violão, ler, desenhar, cozinhar…",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "Marque quando praticar. As mensagens usarão isso: \"seu hobby está há X dias sem sua atenção\".",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(hobbies, key = { it.id }) { hobby ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(hobby.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        PhraseBank.idleText(hobby.lastPracticedAt),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { vm.deleteHobby(hobby) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Excluir",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { vm.practiceHobby(hobby) },
                                    modifier = Modifier.weight(1f)
                                ) { Text("PRATIQUEI AGORA") }
                                TextButton(
                                    onClick = { editing = hobby; showDialog = true }
                                ) { Text("Editar") }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        var text by remember(editing) { mutableStateOf(editing?.text ?: "") }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (editing == null) "Novo hobby" else "Editar hobby") },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Ex.: Tocar violão") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (text.isNotBlank()) {
                        vm.saveHobby(
                            id = editing?.id ?: 0,
                            text = text,
                            lastPracticedAt = editing?.lastPracticedAt ?: 0L
                        )
                        showDialog = false
                    }
                }) { Text("SALVAR") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        )
    }
}
