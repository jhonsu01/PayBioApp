package com.local.paybio.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.local.paybio.backup.BackupManager
import com.local.paybio.util.PrefsManager
import com.local.paybio.util.ShareUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenKiosk: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { PrefsManager(context) }
    var hasPin by remember { mutableStateOf(prefs.hasPin) }
    var showPinDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
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
            modifier = Modifier.padding(inner).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionCard(
                title = "Modo Kiosco",
                body = if (hasPin) "PIN configurado. Solo se podrá salir del Modo Kiosco con el PIN."
                else "Sin PIN. Configura uno para bloquear ediciones mientras expones tus QR."
            ) {
                Button(onClick = { showPinDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Lock, contentDescription = null)
                    Text(if (hasPin) "  Cambiar PIN" else "  Configurar PIN")
                }
                if (hasPin) {
                    OutlinedButton(
                        onClick = { prefs.clearPin(); hasPin = false },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Quitar PIN") }
                }
                OutlinedButton(onClick = onOpenKiosk, modifier = Modifier.fillMaxWidth()) {
                    Text("Iniciar Modo Kiosco")
                }
            }

            SectionCard(
                title = "Respaldo Cero-Nube",
                body = "Empaqueta la base de datos cifrada y tus imágenes QR en un .zip. Tú decides dónde guardarlo (Drive, mensajería, USB). PayBio no usa servidores."
            ) {
                Button(
                    onClick = {
                        val zip = BackupManager.createBackupZip(context)
                        if (zip != null) {
                            ShareUtil.shareFile(context, zip, "application/zip", "Respaldo PayBio")
                        } else {
                            Toast.makeText(context, "No hay datos para respaldar.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Archive, contentDescription = null)
                    Text("  Exportar respaldo (.zip)")
                }
            }

            SectionCard(
                title = "Privacidad",
                body = "PayBio funciona 100% sin conexión. No se solicita el permiso de Internet. Tus datos nunca salen del dispositivo salvo que tú los compartas."
            ) {}
        }
    }

    if (showPinDialog) {
        PinDialog(
            onConfirm = { pin -> prefs.kioskPin = pin; hasPin = true; showPinDialog = false },
            onDismiss = { showPinDialog = false }
        )
    }
}

@Composable
private fun SectionCard(title: String, body: String, content: @Composable () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Text(body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            content()
        }
    }
}

@Composable
private fun PinDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    val valid = pin.length in 4..6
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("PIN del Modo Kiosco") },
        text = {
            Column {
                Text("Introduce un PIN de 4 a 6 dígitos.")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) pin = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(pin) }, enabled = valid) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
