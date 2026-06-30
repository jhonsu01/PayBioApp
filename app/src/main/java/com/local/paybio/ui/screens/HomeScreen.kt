package com.local.paybio.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.local.paybio.R
import com.local.paybio.ui.PaymentViewModel
import com.local.paybio.ui.components.PaymentCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PaymentViewModel,
    onAddManual: () -> Unit,
    onIngest: () -> Unit,
    onOpenDetail: (Int) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenKiosk: () -> Unit
) {
    val methods by viewModel.methods.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("PayBio", style = MaterialTheme.typography.headlineLarge)
                        Text(
                            "Smart Offline Ledger",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onIngest) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = "Ingesta IA", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onOpenKiosk) {
                        Icon(Icons.Filled.Lock, contentDescription = "Modo Kiosco")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Ajustes")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddManual,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nueva tarjeta") }
            )
        }
    ) { inner ->
        if (methods.isEmpty()) {
            EmptyState(Modifier.padding(inner).fillMaxSize(), onIngest)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().focusGroup(),
                contentPadding = PaddingValues(16.dp, inner.calculateTopPadding(), 16.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(methods, key = { it.id }) { method ->
                    PaymentCard(
                        method = method,
                        onClick = { onOpenDetail(method.id) },
                        onToggleFavorite = { viewModel.toggleFavorite(method) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier, onIngest: () -> Unit) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.logo_paybio),
                contentDescription = null,
                modifier = Modifier.size(96.dp)
            )
            Text(
                "Aún no tienes métodos de cobro",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                "Usa la Ingesta IA para capturar una dirección\ndesde una captura de pantalla o texto pegado.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, start = 24.dp, end = 24.dp)
            )
            ExtendedFloatingActionButton(
                onClick = onIngest,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Filled.AutoAwesome, contentDescription = null) },
                text = { Text("Ingesta inteligente") },
                modifier = Modifier.padding(top = 24.dp)
            )
        }
    }
}
