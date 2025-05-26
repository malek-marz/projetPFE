package com.example.testapp.features.chs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportUserScreen(
    reportedUserUid: String,
    onReportSent: (() -> Unit)? = null,
    viewModel: ReportUserViewModel = viewModel()
) {
    val reasons = listOf(
        "Comportement inapproprié",
        "Problèmes de communication",
        "Annulation fréquente",
        "Fausse identité",
        "Ne respecte pas les horaires",
        "Demandes suspectes",
        "Spam ou publicité",
        "Discours haineux",
        "Autre"
    )

    var selectedReasons by remember { mutableStateOf(setOf<String>()) }
    var additionalComments by remember { mutableStateOf(TextFieldValue("")) }
    var confirmationShown by remember { mutableStateOf(false) }
    var sendingInProgress by remember { mutableStateOf(false) }
    var sendSuccess by remember { mutableStateOf<Boolean?>(null) }

    var showBlockMuteDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    fun toggleReason(reason: String) {
        selectedReasons = if (selectedReasons.contains(reason)) {
            selectedReasons - reason
        } else {
            if (selectedReasons.size < 3) selectedReasons + reason else selectedReasons
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Signaler un utilisateur") },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = Color(0xFFD32F2F),
                    titleContentColor = Color.White
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                "Pourquoi souhaitez-vous signaler cet utilisateur ? (1 à 3 choix)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            reasons.forEach { reason ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { toggleReason(reason) }
                        .background(
                            if (selectedReasons.contains(reason)) Color(0xFFFFCDD2) else Color.Transparent
                        )
                        .padding(12.dp)
                ) {
                    Text(reason)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Commentaires supplémentaires (optionnel) :",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = additionalComments,
                onValueChange = { additionalComments = it },
                placeholder = { Text("Expliquez votre signalement ici...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    sendingInProgress = true
                    viewModel.sendReport(
                        reportedUserUid = reportedUserUid,
                        reasons = selectedReasons.toList(),
                        additionalComments = additionalComments.text
                    ) { success ->
                        sendingInProgress = false
                        sendSuccess = success
                        if (success) {
                            confirmationShown = true
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    "Erreur lors de l'envoi du signalement.",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                },
                enabled = selectedReasons.isNotEmpty() && !sendingInProgress,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F),
                    contentColor = Color.White
                )
            ) {
                if (sendingInProgress) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Envoyer le signalement")
                }
            }
        }
    }

    // Confirmation dialog after successful report
    if (confirmationShown) {
        AlertDialog(
            onDismissRequest = { confirmationShown = false },
            confirmButton = {
                TextButton(onClick = {
                    confirmationShown = false
                    showBlockMuteDialog = true
                    onReportSent?.invoke()
                }) {
                    Text("OK")
                }
            },
            title = { Text("Merci pour votre signalement") },
            text = { Text("Nous allons examiner ce signalement rapidement.") }
        )
    }

    // Block / Mute dialog shown after report confirmation
    if (showBlockMuteDialog) {
        AlertDialog(
            onDismissRequest = { showBlockMuteDialog = false },
            title = { Text("Que souhaitez-vous faire ?") },
            text = { Text("Voulez-vous aussi bloquer ou couper le son de cet utilisateur ?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.blockUser(reportedUserUid) { success ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                if (success) "Utilisateur bloqué." else "Échec du blocage.",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                    showBlockMuteDialog = false
                }) {
                    Text("🚫 Bloquer")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        viewModel.muteUser(reportedUserUid) { success ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    if (success) "Utilisateur coupé." else "Échec de la coupure.",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                        showBlockMuteDialog = false
                    }) {
                        Text("🔕 Couper le son")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { showBlockMuteDialog = false }) {
                        Text("Annuler")
                    }
                }
            }
        )
    }
}
