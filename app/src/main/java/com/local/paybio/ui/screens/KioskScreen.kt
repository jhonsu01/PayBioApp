package com.local.paybio.ui.screens

import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.local.paybio.data.PaymentMethod
import com.local.paybio.ui.PaymentViewModel
import com.local.paybio.ui.components.CardQr
import com.local.paybio.ui.components.LogoAvatar
import com.local.paybio.ui.components.parseColor
import com.local.paybio.util.PrefsManager
import com.local.paybio.util.findActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KioskScreen(
    viewModel: PaymentViewModel,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val activity = remember { context.findActivity() }
    val prefs = remember { PrefsManager(context) }
    val all by viewModel.methods.collectAsState()
    val cards = remember(all) { all.filter { it.isFavorite }.ifEmpty { all } }

    var showPin by remember { mutableStateOf(false) }
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    // Anti-reposo: keep the screen on and lock the app in the foreground.
    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        runCatching { activity?.startLockTask() }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            runCatching { activity?.stopLockTask() }
        }
    }

    fun requestExit() {
        if (prefs.hasPin) { pin = ""; error = false; showPin = true } else onExit()
    }

    BackHandler { requestExit() }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { inner ->
        Box(Modifier.padding(inner).fillMaxSize()) {
            if (cards.isEmpty()) {
                Text(
                    "Agrega tarjetas para mostrarlas en Modo Kiosco.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp)
                )
            } else {
                val pagerState = rememberPagerState(pageCount = { cards.size })
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    KioskCard(cards[page], page + 1, cards.size)
                }
            }

            IconButton(
                onClick = { requestExit() },
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Salir del Modo Kiosco", tint = MaterialTheme.colorScheme.onBackground)
            }
        }
    }

    if (showPin) {
        AlertDialog(
            onDismissRequest = { showPin = false },
            title = { Text("Salir del Modo Kiosco") },
            text = {
                Column {
                    Text("Introduce el PIN para salir.")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) { pin = it; error = false } },
                        isError = error,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (error) Text("PIN incorrecto", color = MaterialTheme.colorScheme.error)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (prefs.checkPin(pin)) { showPin = false; onExit() } else error = true
                }) { Text("Salir") }
            },
            dismissButton = { TextButton(onClick = { showPin = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun KioskCard(method: PaymentMethod, index: Int, total: Int) {
    val accent = parseColor(method.colorHex)
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LogoAvatar(method, accent, 64)
        Spacer(Modifier.height(12.dp))
        Text(method.displayName, style = MaterialTheme.typography.headlineLarge, color = accent)
        Text("${method.country} · ${method.type}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        CardQr(method = method, sizeDp = 300)
        Spacer(Modifier.height(24.dp))
        Text(method.holderName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        Text(
            method.accountNumber,
            fontFamily = FontFamily.Monospace,
            color = accent,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("$index / $total", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
