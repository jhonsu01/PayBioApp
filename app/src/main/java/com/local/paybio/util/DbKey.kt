package com.local.paybio.util

import android.content.Context
import android.util.Base64
import java.security.SecureRandom

/**
 * Per-install passphrase for the SQLCipher database.
 * A random 256-bit key is generated on first launch and persisted locally.
 * (Hardening note: wrap with Android Keystore / EncryptedSharedPreferences
 * for production-grade protection.)
 */
object DbKey {
    private const val PREFS = "paybio_secure"
    private const val KEY = "db_key"

    fun getOrCreate(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.getString(KEY, null)?.let { return it }
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        val key = Base64.encodeToString(bytes, Base64.NO_WRAP)
        prefs.edit().putString(KEY, key).apply()
        return key
    }
}
