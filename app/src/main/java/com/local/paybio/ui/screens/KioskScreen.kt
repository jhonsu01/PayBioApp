package com.local.paybio.ui.screens

import android.content.pm.PackageManager
import android.content.res.Configuration
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as columnItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.local.paybio.data.PaymentMethod
import com.local.paybio.ui.PaymentViewModel
import com.local.paybio.ui.components.CardQr
import com.local.paybio.ui.components.KioskLogo
import com.local.paybio.ui.components.LogoAvatar
import com.local.paybio.ui.components.parseColor
import com.local.paybio.util.PrefsManager
import com.local.paybio.util.findActivity
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sqrt

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

    val config = LocalConfiguration.current
    val pm = context.packageManager
    val isTv = pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK) ||
        pm.hasSystemFeature(PackageManager.FEATURE_TELEVISION) ||
        (config.uiMode and Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_TELEVISION
    val isTouch = pm.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)
    val showAll = isTv || !isTouch || config.screenWidthDp >= 600

    var selected by remember(cards) { mutableStateOf(if (cards.size == 1) cards.first() else null) }

    var showPin by remember { mutableStateOf(false) }
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

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

    BackHandler {
        if (!showAll && selected != null && cards.size > 1) selected = null else requestExit()
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { inner ->
        Box(Modifier.padding(inner).fillMaxSize()) {
            when {
                cards.isEmpty() -> Text(
                    "Agrega tarjetas para mostrarlas en Modo Kiosco.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp)
                )
                // TV / non-touch / large: ALL methods fit on screen at once (no scroll).
                showAll -> KioskAllGrid(cards, Modifier.padding(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 16.dp))
                // Small touch screen: menu first, then the selected method.
                selected == null -> KioskMenu(cards, Modifier.padding(top = 48.dp)) { selected = it }
                else -> KioskDetail(method = selected!!, showBack = cards.size > 1, onBack = { selected = null })
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

/** Non-scrolling adaptive grid: computes rows/cols from the count and scales each
 *  tile (logo + QR) so that EVERY method fits on a large screen / TV at once. */
@Composable
private fun KioskAllGrid(cards: List<PaymentMethod>, modifier: Modifier = Modifier) {
    val n = cards.size
    val rows = floor(sqrt(n.toFloat())).toInt().coerceAtLeast(1)
    val cols = ceil(n / rows.toFloat()).toInt().coerceAtLeast(1)
    val gap = 12.dp
    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(gap)) {
        for (r in 0 until rows) {
            Row(
                Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(gap)
            ) {
                for (c in 0 until cols) {
                    val idx = r * cols + c
                    if (idx < n) {
                        Box(Modifier.weight(1f).fillMaxHeight()) { KioskTileFit(cards[idx]) }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/** A single tile that measures its cell and sizes the QR/logo to fit (no overflow). */
@Composable
private fun KioskTileFit(method: PaymentMethod) {
    val accent = parseColor(method.colorHex)
    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        BoxWithConstraints(Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.Center) {
            val side = minOf(maxWidth, maxHeight).value
            val qr = (side * 0.38f).toInt().coerceIn(48, 280)
            val logo = (side * 0.12f).toInt().coerceIn(22, 56)
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                KioskLogo(method, accent, logo)
                Spacer(Modifier.height(4.dp))
                Text(
                    method.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    color = accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Text(
                    "${method.country} · ${method.type}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                CardQr(method = method, sizeDp = qr)
                Spacer(Modifier.height(6.dp))
                Text(
                    method.holderName,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    method.accountNumber,
                    fontFamily = FontFamily.Monospace,
                    color = accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Phone menu: a "Medios de pago" list to choose which card to display. */
@Composable
private fun KioskMenu(cards: List<PaymentMethod>, modifier: Modifier = Modifier, onSelect: (PaymentMethod) -> Unit) {
    Column(modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "Medios de pago",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            columnItems(cards, key = { it.id }) { method ->
                val accent = parseColor(method.colorHex)
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onSelect(method) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        LogoAvatar(method, accent, 52)
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(method.displayName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                            Text("${method.country} · ${method.type}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

/** Single payment method, full screen, with a large logo. */
@Composable
private fun KioskDetail(method: PaymentMethod, showBack: Boolean, onBack: () -> Unit) {
    val accent = parseColor(method.colorHex)
    Box(Modifier.fillMaxSize()) {
        if (showBack) {
            TextButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart).padding(8.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = accent)
                Text("  Medios de pago", color = accent)
            }
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            KioskLogo(method, accent, 120)
            Spacer(Modifier.height(12.dp))
            Text(method.displayName, style = MaterialTheme.typography.headlineLarge, color = accent, textAlign = TextAlign.Center)
            Text("${method.country} · ${method.type}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(20.dp))
            CardQr(method = method, sizeDp = 300)
            Spacer(Modifier.height(20.dp))
            Text(method.holderName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center)
            Text(
                method.accountNumber,
                fontFamily = FontFamily.Monospace,
                color = accent,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
        }
    }
}
