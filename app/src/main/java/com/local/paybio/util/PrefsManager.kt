package com.local.paybio.util

import android.content.Context

/** Lightweight preferences for kiosk mode and security PIN. */
class PrefsManager(context: Context) {
    private val prefs = context.getSharedPreferences("paybio_prefs", Context.MODE_PRIVATE)

    var kioskPin: String?
        get() = prefs.getString(KEY_PIN, null)
        set(value) = prefs.edit().putString(KEY_PIN, value).apply()

    val hasPin: Boolean get() = !kioskPin.isNullOrBlank()

    fun checkPin(input: String): Boolean = kioskPin != null && kioskPin == input

    fun clearPin() = prefs.edit().remove(KEY_PIN).apply()

    companion object {
        private const val KEY_PIN = "kiosk_pin"
    }
}
