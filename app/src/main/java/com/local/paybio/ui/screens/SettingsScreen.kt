package com.local.paybio.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.rememberCoroutineScope
import com.local.paybio.backup.BackupManager
import com.local.paybio.util.PrefsManager
import com.local.paybio.util.ShareUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenKiosk: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { PrefsManager(context) }
    var hasPin by remember { mutableStateOf(prefs.hasPin) }
    var showPinDialog by remember { mutableStateOf(false) }
    var pendingImport by remember { mutableStateOf<Uri?>(null) }
    var importing by remember { mutableStateOf(false) }
    var showRestart by remember { mutableStateOf(false) }
    var showExportPinChoice by remember { mutableStateOf(false) }
    var showImportChoice by remember { mutableStateOf(false) }
    var showBrowser by remember { mutableStateOf(false) }

    val importPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) pendingImport = uri
    }

    fun doExport(includePin: Boolean) {
        val zip = BackupManager.createBackupZip(context, includePin)
        if (zip != null) {
            ShareUtil.shareFile(context, zip, "application/zip", "Respaldo PayBio")
        } else {
            Toast.makeText(context, "No hay datos para respaldar.", Toast.LENGTH_SHORT).show()
        }
    }

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
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
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
                body = "Empaqueta la base cifrada, tus imágenes (QR/logos) y la clave en un .zip. Tú decides dónde guardarlo (Drive, mensajería, USB). El respaldo incluye la clave para poder restaurarlo: guárdalo en un lugar seguro."
            ) {
                Button(
                    onClick = { if (prefs.hasPin) showExportPinChoice = true else doExport(false) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Archive, contentDescription = null)
                    Text("  Exportar respaldo (.zip)")
                }
                OutlinedButton(
                    onClick = { showImportChoice = true },
                    enabled = !importing,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Unarchive, contentDescription = null)
                    Text(if (importing) "  Importando…" else "  Importar respaldo (.zip)")
                }
            }

            SectionCard(
                title = "Privacidad",
                body = "PayBio funciona 100% sin conexión. No se solicita el permiso de Internet. Tus datos nunca salen del dispositivo salvo que tú los compartas."
            ) {}

            SectionCard(
                title = "Apoya el proyecto ☕",
                body = "PayBio es gratuito y sin anuncios. Si te sirve, puedes invitarme un café en Ko-fi. Se abre en tu navegador (la app sigue sin acceso a Internet)."
            ) {
                Button(
                    onClick = {
                        runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://ko-fi.com/V7V81LV7GX"))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }.onFailure {
                            Toast.makeText(context, "No se pudo abrir el navegador.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5E5B), contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Favorite, contentDescription = null)
                    Text("  Apóyame en Ko-fi")
                }
            }
        }
    }

    if (showPinDialog) {
        PinDialog(
            onConfirm = { pin -> prefs.kioskPin = pin; hasPin = true; showPinDialog = false },
            onDismiss = { showPinDialog = false }
        )
    }

    pendingImport?.let { uri ->
        AlertDialog(
            onDismissRequest = { if (!importing) pendingImport = null },
            title = { Text("Importar respaldo") },
            text = { Text("Esto reemplazará TODAS tus tarjetas actuales con las del respaldo. ¿Continuar?") },
            confirmButton = {
                TextButton(
                    enabled = !importing,
                    onClick = {
                        importing = true
                        scope.launch {
                            val ok = withContext(Dispatchers.IO) { BackupManager.importBackupZip(context, uri) }
                            importing = false
                            pendingImport = null
                            if (ok) showRestart = true
                            else Toast.makeText(context, "No se pudo importar (¿zip válido?).", Toast.LENGTH_LONG).show()
                        }
                    }
                ) { Text("Importar") }
            },
            dismissButton = { TextButton(enabled = !importing, onClick = { pendingImport = null }) { Text("Cancelar") } }
        )
    }

    if (showRestart) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Respaldo importado") },
            text = { Text("Reinicia la app para cargar los datos restaurados.") },
            confirmButton = { TextButton(onClick = { restartApp(context) }) { Text("Reiniciar ahora") } }
        )
    }

    if (showExportPinChoice) {
        AlertDialog(
            onDismissRequest = { showExportPinChoice = false },
            title = { Text("¿Incluir el PIN del Kiosco?") },
            text = { Text("Puedes incluir el PIN del Modo Kiosco en el respaldo para recuperarlo al restaurar, o exportar sin él.") },
            confirmButton = { TextButton(onClick = { showExportPinChoice = false; doExport(true) }) { Text("Con PIN") } },
            dismissButton = { TextButton(onClick = { showExportPinChoice = false; doExport(false) }) { Text("Sin PIN") } }
        )
    }

    if (showImportChoice) {
        AlertDialog(
            onDismissRequest = { showImportChoice = false },
            title = { Text("Buscar el respaldo") },
            text = { Text("Elige cómo localizar el archivo .zip. Si tu dispositivo (p. ej. una TV) no abre el selector del sistema, usa el explorador integrado.") },
            confirmButton = {
                TextButton(onClick = {
                    showImportChoice = false
                    importPicker.launch(arrayOf("application/zip", "application/octet-stream", "*/*"))
                }) { Text("Selector del sistema") }
            },
            dismissButton = {
                TextButton(onClick = { showImportChoice = false; showBrowser = true }) { Text("Explorador integrado") }
            }
        )
    }

    if (showBrowser) {
        Dialog(
            onDismissRequest = { showBrowser = false },
            properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true)
        ) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                FileBrowserScreen(
                    onPick = { file -> showBrowser = false; pendingImport = android.net.Uri.fromFile(file) },
                    onClose = { showBrowser = false }
                )
            }
        }
    }
}

private fun restartApp(context: Context) {
    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    context.startActivity(intent)
    Runtime.getRuntime().exit(0)
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
