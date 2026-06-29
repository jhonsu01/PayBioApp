package com.local.paybio.ui.screens

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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.local.paybio.data.PaymentMethod
import com.local.paybio.ui.PaymentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    viewModel: PaymentViewModel,
    methodId: Int,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val catalog = viewModel.catalog

    var country by remember { mutableStateOf("") }
    var platform by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("#00E676") }
    var holderName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var isFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(methodId) {
        if (methodId != -1) {
            viewModel.getById(methodId)?.let { m ->
                country = m.country; platform = m.platformName; type = m.type
                color = m.colorHex; holderName = m.holderName
                accountNumber = m.accountNumber; isFavorite = m.isFavorite
            }
        } else {
            viewModel.pendingPrefill?.let { p ->
                country = p.country; platform = p.platformName; type = p.type
                color = p.colorHex; accountNumber = p.accountNumber
            }
            viewModel.pendingPrefill = null
        }
    }

    val selectedCountry = catalog.find { it.name == country }
    val selectedMethod = selectedCountry?.methods?.find { it.platform == platform }
    // keep type/color in sync with the chosen template
    LaunchedEffect(platform) {
        selectedMethod?.let { type = it.type; color = it.color }
    }

    val accountInvalid = accountNumber.isNotBlank() && selectedMethod != null && !selectedMethod.matches(accountNumber)
    val canSave = country.isNotBlank() && platform.isNotBlank() &&
        holderName.isNotBlank() && accountNumber.isNotBlank() && !accountInvalid

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (methodId == -1) "Nueva tarjeta" else "Editar tarjeta") },
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DropdownField(
                label = "País / Red",
                value = country,
                options = catalog.map { it.name },
                onSelected = { country = it; platform = "" }
            )
            DropdownField(
                label = "Plataforma",
                value = platform,
                options = selectedCountry?.methods?.map { it.platform } ?: emptyList(),
                enabled = selectedCountry != null,
                onSelected = { platform = it }
            )
            OutlinedTextField(
                value = type,
                onValueChange = { type = it },
                label = { Text("Tipo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = holderName,
                onValueChange = { holderName = it },
                label = { Text("Titular") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = accountNumber,
                onValueChange = { accountNumber = it },
                label = { Text("Cuenta / Dirección") },
                isError = accountInvalid,
                supportingText = {
                    Text(
                        if (accountInvalid) "Formato inválido para ${selectedMethod?.platform}"
                        else selectedMethod?.hint ?: "Ingresa la cuenta o dirección de wallet"
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = isFavorite, onCheckedChange = { isFavorite = it })
                Spacer(Modifier.width(8.dp))
                Text("Marcar como favorita", color = MaterialTheme.colorScheme.onBackground)
            }
            Button(
                onClick = {
                    viewModel.save(
                        PaymentMethod(
                            id = if (methodId == -1) 0 else methodId,
                            country = country,
                            platformName = platform,
                            type = type.ifBlank { "Otro" },
                            holderName = holderName,
                            accountNumber = accountNumber.trim(),
                            colorHex = color,
                            isFavorite = isFavorite
                        )
                    )
                    onDone()
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (methodId == -1) "Guardar tarjeta" else "Actualizar")
            }
            Spacer(Modifier.width(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    value: String,
    options: List<String>,
    enabled: Boolean = true,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded && enabled, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onSelected(option); expanded = false }
                )
            }
        }
    }
}
