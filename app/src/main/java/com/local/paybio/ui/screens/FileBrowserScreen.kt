package com.local.paybio.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.NorthWest
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.io.File

private val IMAGE_EXTS = listOf("jpg", "jpeg", "png", "webp", "gif", "bmp")

private fun hasStorageAccess(context: android.content.Context, needWrite: Boolean): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        val perm = if (needWrite) Manifest.permission.WRITE_EXTERNAL_STORAGE else Manifest.permission.READ_EXTERNAL_STORAGE
        ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
    }

private fun listEntries(dir: File, extensions: List<String>?): List<File> =
    runCatching {
        (dir.listFiles()?.toList() ?: emptyList())
            .filter { f ->
                !f.isHidden && (f.isDirectory ||
                    (extensions != null && extensions.any { f.name.endsWith(".$it", ignoreCase = true) }))
            }
            .sortedWith(compareByDescending<File> { it.isDirectory }.thenBy { it.name.lowercase() })
    }.getOrDefault(emptyList())

/**
 * Minimal built-in file browser (inspired by Fossify File Manager). Navigable with
 * touch and with a D-pad / remote (focusable rows).
 *
 * - [extensions] = list of accepted file extensions to PICK a file (e.g. ["zip"]).
 * - [extensions] = null -> FOLDER picker: shows only folders + a "Guardar aquí" action.
 */
@Composable
fun FileBrowserScreen(
    title: String,
    extensions: List<String>?,
    onPick: (File) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val needWrite = extensions == null
    var granted by remember { mutableStateOf(hasStorageAccess(context, needWrite)) }

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        granted = ok
    }
    val settingsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        granted = hasStorageAccess(context, needWrite)
    }

    fun requestAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = runCatching {
                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:${context.packageName}"))
            }.getOrElse { Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION) }
            runCatching { settingsLauncher.launch(intent) }
        } else {
            permLauncher.launch(if (needWrite) Manifest.permission.WRITE_EXTERNAL_STORAGE else Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
            IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = "Cerrar") }
        }
        Spacer(Modifier.height(12.dp))

        if (!granted) {
            Text(
                "Para acceder al almacenamiento, concede acceso a archivos. " +
                    "Solo se usa para esta operación; la app sigue sin conexión a Internet.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = { requestAccess() }, modifier = Modifier.fillMaxWidth()) { Text("Conceder acceso a archivos") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
            return@Column
        }

        var current by remember { mutableStateOf(Environment.getExternalStorageDirectory() ?: File("/storage/emulated/0")) }
        val entries = remember(current.path) { listEntries(current, extensions) }

        if (needWrite) {
            Button(onClick = { onPick(current) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Text("  Guardar en esta carpeta")
            }
            Spacer(Modifier.height(8.dp))
        }

        Text(current.path, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val parent = current.parentFile
            if (parent != null && current.path != "/") {
                item { EntryRow(icon = Icons.Filled.NorthWest, name = "..  (subir)", onClick = { current = parent }) }
            }
            if (entries.isEmpty()) {
                item {
                    Text(
                        if (needWrite) "(sin subcarpetas)" else "(carpeta vacía o sin archivos compatibles)",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
            items(entries, key = { it.path }) { f ->
                when {
                    f.isDirectory -> EntryRow(Icons.Filled.Folder, f.name) { current = f }
                    f.name.endsWith(".zip", true) -> EntryRow(Icons.Filled.FolderZip, f.name, accent = true) { onPick(f) }
                    IMAGE_EXTS.any { f.name.endsWith(".$it", true) } -> EntryRow(Icons.Filled.Image, f.name, accent = true) { onPick(f) }
                    else -> EntryRow(Icons.Filled.Description, f.name, accent = true) { onPick(f) }
                }
            }
        }
    }
}

@Composable
private fun EntryRow(icon: ImageVector, name: String, accent: Boolean = false, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = if (accent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(14.dp))
            Text(
                name,
                color = if (accent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
