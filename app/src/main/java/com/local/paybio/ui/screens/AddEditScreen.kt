package com.local.paybio.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.text.KeyboardOptions
import coil.compose.AsyncImage
import com.local.paybio.data.PaymentMethod
import com.local.paybio.ui.PaymentViewModel
import com.local.paybio.util.ImageStore
import java.io.File

private const val OTHER_COUNTRY = "➕ Otro país…"
private const val OTHER_PLATFORM = "➕ Otra plataforma…"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    viewModel: PaymentViewModel,
    methodId: Int,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val catalog = viewModel.catalog

    var country by remember { mutableStateOf("") }
    var platform by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("#00E676") }
    var holderName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var isFavorite by remember { mutableStateOf(false) }
    var logoPath by remember { mutableStateOf<String?>(null) }
    var qrPath by remember { mutableStateOf<String?>(null) }
    var customCountry by remember { mutableStateOf(false) }
    var customPlatform by remember { mutableStateOf(false) }

    LaunchedEffect(methodId) {
        if (methodId != -1) {
            viewModel.getById(methodId)?.let { m ->
                country = m.country; platform = m.platformName; type = m.type
                color = m.colorHex; holderName = m.holderName
                accountNumber = m.accountNumber; isFavorite = m.isFavorite
                label = m.label ?: ""; logoPath = m.logoImagePath; qrPath = m.qrCodeImagePath
                val cat = catalog.find { it.name == m.country }
                customCountry = cat == null
                customPlatform = cat?.methods?.none { it.platform == m.platformName } ?: true
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
    val selectedMethod = if (customPlatform) null else selectedCountry?.methods?.find { it.platform == platform }
    LaunchedEffect(platform, customPlatform) {
        if (!customPlatform) selectedMethod?.let { type = it.type; color = it.color }
    }

    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { logoPath = ImageStore.save(context, it, "logos", "logo") }
    }
    val qrPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { qrPath = ImageStore.save(context, it, "qrs", "qr") }
    }
    // "logo" / "qr": which image we're picking (choice dialog), and which uses the internal browser.
    var pickTarget by remember { mutableStateOf<String?>(null) }
    var browseTarget by remember { mutableStateOf<String?>(null) }

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
            // --- País / Red ---
            DropdownField(
                label = "País / Red",
                value = if (customCountry) OTHER_COUNTRY else country,
                options = catalog.map { it.name } + OTHER_COUNTRY,
                onSelected = { sel ->
                    if (sel == OTHER_COUNTRY) {
                        customCountry = true; country = ""; platform = ""; customPlatform = true
                    } else {
                        customCountry = false; country = sel; platform = ""; customPlatform = false
                    }
                }
            )
            if (customCountry) {
                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("Nombre del país / red") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // --- Plataforma ---
            if (customCountry || customPlatform) {
                OutlinedTextField(
                    value = platform,
                    onValueChange = { platform = it },
                    label = { Text("Plataforma (personalizada)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (!customCountry) {
                    TextButton(onClick = { customPlatform = false; platform = "" }) {
                        Text("Elegir de la lista")
                    }
                }
            } else {
                DropdownField(
                    label = "Plataforma",
                    value = platform,
                    options = (selectedCountry?.methods?.map { it.platform } ?: emptyList()) + OTHER_PLATFORM,
                    enabled = selectedCountry != null,
                    onSelected = { sel ->
                        if (sel == OTHER_PLATFORM) { customPlatform = true; platform = "" }
                        else platform = sel
                    }
                )
            }

            OutlinedTextField(
                value = type,
                onValueChange = { type = it },
                label = { Text("Tipo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Nombre de la tarjeta (opcional)") },
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

            // --- Logo personalizado ---
            ImagePickerRow(
                title = "Logo del banco / servicio (opcional)",
                path = logoPath,
                icon = { Icon(Icons.Filled.Image, contentDescription = null) },
                onPick = { pickTarget = "logo" },
                onClear = { logoPath = null }
            )
            // --- QR personalizado ---
            ImagePickerRow(
                title = "QR personalizado (opcional, reemplaza al generado)",
                path = qrPath,
                icon = { Icon(Icons.Filled.QrCode2, contentDescription = null) },
                onPick = { pickTarget = "qr" },
                onClear = { qrPath = null }
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
                            country = country.trim(),
                            platformName = platform.trim(),
                            type = type.ifBlank { "Otro" },
                            holderName = holderName,
                            accountNumber = accountNumber.trim(),
                            qrCodeImagePath = qrPath,
                            logoImagePath = logoPath,
                            label = label.ifBlank { null },
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

    // Choice: external gallery app vs the built-in browser (for TVs without a gallery).
    pickTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { pickTarget = null },
            title = { Text("Seleccionar imagen") },
            text = { Text("Elige cómo buscar la imagen. En TV sin galería, usa el explorador interno.") },
            confirmButton = {
                TextButton(onClick = {
                    pickTarget = null
                    if (target == "logo") logoPicker.launch("image/*") else qrPicker.launch("image/*")
                }) { Text("App externa") }
            },
            dismissButton = {
                TextButton(onClick = { browseTarget = target; pickTarget = null }) { Text("Explorador interno") }
            }
        )
    }

    browseTarget?.let { target ->
        Dialog(
            onDismissRequest = { browseTarget = null },
            properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true)
        ) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                FileBrowserScreen(
                    title = if (target == "logo") "Buscar logo" else "Buscar QR",
                    extensions = listOf("jpg", "jpeg", "png", "webp", "gif", "bmp"),
                    onPick = { file ->
                        val uri = android.net.Uri.fromFile(file)
                        if (target == "logo") logoPath = ImageStore.save(context, uri, "logos", "logo")
                        else qrPath = ImageStore.save(context, uri, "qrs", "qr")
                        browseTarget = null
                    },
                    onClose = { browseTarget = null }
                )
            }
        }
    }
}

@Composable
private fun ImagePickerRow(
    title: String,
    path: String?,
    icon: @Composable () -> Unit,
    onPick: () -> Unit,
    onClear: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (path != null && File(path).exists()) {
            AsyncImage(
                model = File(path),
                contentDescription = null,
                modifier = Modifier.size(44.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(180.dp))
            Spacer(Modifier.width(4.dp))
            TextButton(onClick = onClear) { Text("Quitar") }
        } else {
            OutlinedButton(onClick = onPick, modifier = Modifier.fillMaxWidth()) {
                icon()
                Spacer(Modifier.width(8.dp))
                Text(title)
            }
        }
    }
}

/**
 * Dialog-based picker: a focusable field that opens a list dialog. Works with
 * touch AND with a TV remote / D-pad (each option is a focusable button), unlike
 * the anchored ExposedDropdownMenu which was hard to navigate on Android TV.
 */
@Composable
private fun DropdownField(
    label: String,
    value: String,
    options: List<String>,
    enabled: Boolean = true,
    onSelected: (String) -> Unit
) {
    var open by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedButton(
            onClick = { if (enabled) open = true },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                value.ifBlank { "Seleccionar…" },
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start,
                maxLines = 1
            )
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        }
    }
    if (open) {
        AlertDialog(
            onDismissRequest = { open = false },
            title = { Text(label) },
            text = {
                LazyColumn {
                    items(options) { option ->
                        TextButton(
                            onClick = { onSelected(option); open = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(option, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { open = false }) { Text("Cerrar") } }
        )
    }
}
