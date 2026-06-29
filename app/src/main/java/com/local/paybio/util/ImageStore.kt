package com.local.paybio.util

import android.content.Context
import android.net.Uri
import java.io.File

/** Copies user-picked images (logos / custom QR) into app-internal storage. */
object ImageStore {

    fun dir(context: Context, sub: String): File =
        File(context.filesDir, sub).apply { if (!exists()) mkdirs() }

    /**
     * Persists the content at [uri] into filesDir/[sub] and returns the absolute path,
     * or null on failure.
     */
    fun save(context: Context, uri: Uri, sub: String, prefix: String): String? = runCatching {
        val target = File(dir(context, sub), "${prefix}_${System.currentTimeMillis()}.img")
        context.contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { out -> input.copyTo(out) }
        } ?: return null
        target.absolutePath
    }.getOrNull()
}
