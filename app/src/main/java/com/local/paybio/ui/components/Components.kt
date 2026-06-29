package com.local.paybio.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.local.paybio.data.PaymentMethod
import com.local.paybio.util.QrGenerator
import java.io.File

fun parseColor(hex: String): Color = runCatching {
    Color(android.graphics.Color.parseColor(hex))
}.getOrDefault(Color(0xFF00E676))

fun maskAccount(value: String): String {
    val v = value.trim()
    if (v.length <= 8) return v
    return "${v.take(4)}…${v.takeLast(4)}"
}

@Composable
fun PaymentCard(
    method: PaymentMethod,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = parseColor(method.colorHex)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LogoAvatar(method, accent, 48)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    method.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "${method.country} · ${method.type}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    maskAccount(method.accountNumber),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    color = accent
                )
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (method.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Favorito",
                    tint = if (method.isFavorite) accent else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun QrView(content: String, sizeDp: Int = 240, modifier: Modifier = Modifier) {
    val bmp = androidx.compose.runtime.remember(content) { QrGenerator.generate(content) }
    if (bmp != null) {
        Box(
            modifier = modifier
                .size((sizeDp + 10).dp)
                .background(Color.White, RoundedCornerShape(10.dp))
                .padding(5.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "Código QR",
                modifier = Modifier.size(sizeDp.dp)
            )
        }
    }
}

/** Avatar: the user-provided logo if any, otherwise the platform initial. */
@Composable
fun LogoAvatar(method: PaymentMethod, accent: Color, sizeDp: Int) {
    val shape = RoundedCornerShape(14.dp)
    val logo = method.logoImagePath
    if (logo != null && File(logo).exists()) {
        AsyncImage(
            model = File(logo),
            contentDescription = "Logo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(sizeDp.dp).clip(shape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(sizeDp.dp)
                .background(accent.copy(alpha = 0.18f), shape)
                .border(1.dp, accent, shape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = method.displayName.take(1).uppercase(),
                color = accent,
                fontWeight = FontWeight.Bold,
                fontSize = (sizeDp * 0.46f).sp
            )
        }
    }
}

/** Larger logo for Kiosk mode: fits the full image (no crop), falls back to the initial. */
@Composable
fun KioskLogo(method: PaymentMethod, accent: Color, sizeDp: Int) {
    val shape = RoundedCornerShape(20.dp)
    val logo = method.logoImagePath
    if (logo != null && File(logo).exists()) {
        AsyncImage(
            model = File(logo),
            contentDescription = "Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(sizeDp.dp)
        )
    } else {
        Box(
            modifier = Modifier
                .size(sizeDp.dp)
                .background(accent.copy(alpha = 0.18f), shape)
                .border(1.dp, accent, shape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = method.displayName.take(1).uppercase(),
                color = accent,
                fontWeight = FontWeight.Bold,
                fontSize = (sizeDp * 0.42f).sp
            )
        }
    }
}

/** Shows the custom QR image if the user added one, otherwise generates from the account. */
@Composable
fun CardQr(method: PaymentMethod, sizeDp: Int = 240, modifier: Modifier = Modifier) {
    val custom = method.qrCodeImagePath
    if (custom != null && File(custom).exists()) {
        Box(
            modifier = modifier
                .size((sizeDp + 10).dp)
                .background(Color.White, RoundedCornerShape(10.dp))
                .padding(5.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = File(custom),
                contentDescription = "Código QR",
                modifier = Modifier.size(sizeDp.dp)
            )
        }
    } else {
        QrView(content = method.accountNumber, sizeDp = sizeDp, modifier = modifier)
    }
}
