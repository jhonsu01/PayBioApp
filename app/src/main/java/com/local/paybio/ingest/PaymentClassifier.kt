package com.local.paybio.ingest

import android.content.Context
import com.local.paybio.data.PaymentCatalog

/** A classified candidate ready to become a PaymentMethod. */
data class IngestSuggestion(
    val country: String,
    val platform: String,
    val type: String,
    val color: String,
    val value: String
)

/**
 * Local, deterministic classifier. Matches text/QR candidates against the
 * regex patterns of the precharged catalog (Ethereum 0x..., CLABE, Yape, etc.).
 * No network, no model download.
 */
object PaymentClassifier {

    fun classify(context: Context, rawText: String, qrValues: List<String> = emptyList()): List<IngestSuggestion> {
        val catalog = PaymentCatalog.load(context)
        val candidates = buildCandidates(rawText) + qrValues.map { it.trim() }
        val results = LinkedHashMap<String, IngestSuggestion>()

        for (candidate in candidates) {
            val value = candidate.trim()
            if (value.length < 4) continue
            for (country in catalog) {
                for (method in country.methods) {
                    if (method.matches(value)) {
                        val key = "${method.platform}|$value"
                        results.getOrPut(key) {
                            IngestSuggestion(country.name, method.platform, method.type, method.color, value)
                        }
                    }
                }
            }
        }
        return results.values.toList()
    }

    private fun buildCandidates(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        val set = LinkedHashSet<String>()
        text.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isNotEmpty()) {
                set.add(trimmed)
                // digits-only variant (handles "123 456 789" / "1234-5678")
                val digits = trimmed.filter { it.isDigit() }
                if (digits.length >= 6) set.add(digits)
            }
            // whitespace-separated tokens (crypto addresses, aliases)
            line.split(Regex("\\s+")).forEach { token ->
                val t = token.trim().trim('.', ',', ':', ';')
                if (t.length >= 4) set.add(t)
            }
        }
        return set.toList()
    }
}
