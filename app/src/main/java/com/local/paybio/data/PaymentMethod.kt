package com.local.paybio.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Unified model for both traditional payment methods and decentralized
 * (blockchain) addresses. Stored encrypted at rest via SQLCipher.
 */
@Entity(tableName = "payment_methods")
data class PaymentMethod(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val country: String,            // e.g. "Colombia", "Crypto"
    val platformName: String,       // e.g. "Bancolombia", "Metamask (ETH)"
    val type: String,               // e.g. "Billetera Virtual", "Blockchain"
    val holderName: String,         // account holder
    val accountNumber: String,      // account number or wallet address
    val qrCodeImagePath: String? = null, // local path to a custom QR image (overrides generated)
    val logoImagePath: String? = null,   // local path to a custom logo image
    val label: String? = null,           // custom card name / alias (falls back to platformName)
    val colorHex: String = "#00E676",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    /** Name shown on the card: the user alias if set, otherwise the platform. */
    val displayName: String get() = label?.takeIf { it.isNotBlank() } ?: platformName
}
