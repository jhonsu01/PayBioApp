package com.local.paybio.backup

import android.content.Context
import android.net.Uri
import com.local.paybio.data.PayBioDatabase
import com.local.paybio.util.DbKey
import com.local.paybio.util.PrefsManager
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Zero-Knowledge Backup (Module 4): packages the encrypted Room DB, the saved
 * QR/logo images AND the encryption key into a single .zip, so the backup is
 * portable and can be restored on any device. The OS (ACTION_SEND) decides
 * where it goes — PayBio never touches cloud credentials.
 *
 * Security note: because the zip carries the key, treat the file itself as
 * sensitive (store it somewhere safe).
 */
object BackupManager {

    private const val KEY_ENTRY = "paybio.key"
    private const val PIN_ENTRY = "paybio_pin.txt"

    fun qrImagesDir(context: Context): File =
        File(context.filesDir, "qrs").apply { if (!exists()) mkdirs() }

    fun logosDir(context: Context): File =
        File(context.filesDir, "logos").apply { if (!exists()) mkdirs() }

    fun createBackupZip(context: Context, includePin: Boolean = false): File? {
        val dbFile = context.getDatabasePath(PayBioDatabase.DB_NAME)
        val backupFile = File(context.cacheDir, "PayBio_Backup_${System.currentTimeMillis()}.zip")

        ZipOutputStream(BufferedOutputStream(FileOutputStream(backupFile))).use { zipOut ->
            // Encrypted database (+ WAL/SHM sidecars if present)
            listOf(dbFile, File("${dbFile.path}-wal"), File("${dbFile.path}-shm")).forEach { f ->
                if (f.exists() && f.isFile) {
                    zipOut.putNextEntry(ZipEntry(f.name))
                    f.inputStream().use { it.copyTo(zipOut) }
                    zipOut.closeEntry()
                }
            }
            // Encryption key (needed to restore on another install)
            DbKey.peek(context)?.let { key ->
                zipOut.putNextEntry(ZipEntry(KEY_ENTRY))
                zipOut.write(key.toByteArray(Charsets.UTF_8))
                zipOut.closeEntry()
            }
            // Kiosk PIN — only if the user opted in
            if (includePin) {
                PrefsManager(context).kioskPin?.takeIf { it.isNotBlank() }?.let { pin ->
                    zipOut.putNextEntry(ZipEntry(PIN_ENTRY))
                    zipOut.write(pin.toByteArray(Charsets.UTF_8))
                    zipOut.closeEntry()
                }
            }
            // Saved QR images and logos
            putDir(zipOut, qrImagesDir(context), "qrs")
            putDir(zipOut, logosDir(context), "logos")
        }
        return if (backupFile.exists() && backupFile.length() > 0) backupFile else null
    }

    private fun putDir(zipOut: ZipOutputStream, dir: File, prefix: String) {
        dir.listFiles()?.forEach { file ->
            if (file.isFile) {
                zipOut.putNextEntry(ZipEntry("$prefix/${file.name}"))
                file.inputStream().use { it.copyTo(zipOut) }
                zipOut.closeEntry()
            }
        }
    }

    /**
     * Restores a backup zip. Closes the DB, replaces the database files, key and
     * images, then the caller MUST restart the app so the DB reopens cleanly.
     * Returns true on success.
     */
    fun importBackupZip(context: Context, uri: Uri): Boolean = runCatching {
        val dbFile = context.getDatabasePath(PayBioDatabase.DB_NAME)
        dbFile.parentFile?.mkdirs()

        // Release the current DB before touching its files.
        PayBioDatabase.closeAndReset()

        // Clear existing DB sidecars to avoid WAL/SHM mismatch with the restored db.
        listOf(File("${dbFile.path}-wal"), File("${dbFile.path}-shm")).forEach { if (it.exists()) it.delete() }

        var sawDb = false
        context.contentResolver.openInputStream(uri).use { raw ->
            ZipInputStream(raw!!).use { zin ->
                var entry: ZipEntry? = zin.nextEntry
                while (entry != null) {
                    val name = entry.name
                    val target: File? = when {
                        name == PayBioDatabase.DB_NAME -> { sawDb = true; dbFile }
                        name == "${PayBioDatabase.DB_NAME}-wal" -> File("${dbFile.path}-wal")
                        name == "${PayBioDatabase.DB_NAME}-shm" -> File("${dbFile.path}-shm")
                        name == KEY_ENTRY -> null // handled below
                        name == PIN_ENTRY -> null // handled below
                        name.startsWith("qrs/") -> File(qrImagesDir(context), name.removePrefix("qrs/"))
                        name.startsWith("logos/") -> File(logosDir(context), name.removePrefix("logos/"))
                        else -> null
                    }
                    if (name == KEY_ENTRY) {
                        val key = zin.readBytes().toString(Charsets.UTF_8).trim()
                        if (key.isNotEmpty()) DbKey.set(context, key)
                    } else if (name == PIN_ENTRY) {
                        val pin = zin.readBytes().toString(Charsets.UTF_8).trim()
                        if (pin.isNotEmpty()) PrefsManager(context).kioskPin = pin
                    } else if (target != null && !entry.isDirectory) {
                        target.parentFile?.mkdirs()
                        FileOutputStream(target).use { out -> zin.copyTo(out) }
                    }
                    zin.closeEntry()
                    entry = zin.nextEntry
                }
            }
        }
        sawDb
    }.getOrDefault(false)
}
