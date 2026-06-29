package com.local.paybio.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.local.paybio.data.PaymentMethod
import com.local.paybio.ingest.IngestSuggestion
import com.local.paybio.ui.IngestUiState
import com.local.paybio.ui.PaymentViewModel
import com.local.paybio.ui.components.maskAccount
import com.local.paybio.ui.components.parseColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngestScreen(
    viewModel: PaymentViewModel,
    onUseSuggestion: () -> Unit,
    onBack: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val state by viewModel.ingest.collectAsState()
    var pastedText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.resetIngest() }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.ingestFromImage(it) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Ingesta inteligente") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Sube una captura de tu app bancaria o pega un texto. El motor local (ML Kit + reglas) detecta direcciones cripto y cuentas automáticamente. Sin internet.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )

            Button(
                onClick = { picker.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Image, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Elegir captura de pantalla")
            }

            OutlinedTextField(
                value = pastedText,
                onValueChange = { pastedText = it },
                label = { Text("…o pega el texto aquí") },
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                trailingIcon = {
                    IconButton(onClick = {
                        clipboard.getText()?.let { pastedText = it.text }
                    }) {
                        Icon(Icons.Filled.ContentPaste, contentDescription = "Pegar del portapapeles")
                    }
                }
            )
            OutlinedButton(
                onClick = { viewModel.ingestFromText(pastedText) },
                enabled = pastedText.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Search, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Analizar texto")
            }

            when (val s = state) {
                is IngestUiState.Loading -> Row(
                    Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }

                is IngestUiState.Done -> {
                    if (s.suggestions.isEmpty()) {
                        Text(
                            "No se reconoció ninguna cuenta o dirección. Puedes crearla manualmente.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else {
                        Text(
                            "Detectado (${s.suggestions.size}):",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        s.suggestions.forEach { suggestion ->
                            SuggestionCard(suggestion) {
                                viewModel.pendingPrefill = PaymentMethod(
                                    country = suggestion.country,
                                    platformName = suggestion.platform,
                                    type = suggestion.type,
                                    holderName = "",
                                    accountNumber = suggestion.value,
                                    colorHex = suggestion.color
                                )
                                onUseSuggestion()
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun SuggestionCard(suggestion: IngestSuggestion, onClick: () -> Unit) {
    val accent = parseColor(suggestion.color)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("${suggestion.platform} · ${suggestion.country}", color = accent, style = MaterialTheme.typography.titleLarge)
            Text(maskAccount(suggestion.value), fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurface)
            Text("Toca para crear la tarjeta", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
