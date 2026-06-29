package com.local.paybio.backup

import android.content.Context
import com.local.paybio.data.PayBioDatabase
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Zero-Knowledge Backup (Module 4): packages the encrypted Room DB and any
 * saved QR images into a single .zip. The OS (ACTION_SEND) decides where it
 * goes — PayBio never touches cloud credentials.
 */
object BackupManager {

    fun qrImagesDir(context: Context): File =
        File(context.filesDir, "qrs").apply { if (!exists()) mkdirs() }

    fun createBackupZip(context: Context): File? {
        val dbFile = context.getDatabasePath(PayBioDatabase.DB_NAME)
        val qrDir = qrImagesDir(context)
        val backupFile = File(context.cacheDir, "PayBio_Backup_${System.currentTimeMillis()}.zip")

        ZipOutputStream(BufferedOutputStream(FileOutputStream(backupFile))).use { zipOut ->
            // Main encrypted database (+ WAL/SHM sidecars if present)
            listOf(dbFile, File("${dbFile.path}-wal"), File("${dbFile.path}-shm")).forEach { f ->
                if (f.exists() && f.isFile) {
                    zipOut.putNextEntry(ZipEntry(f.name))
                    f.inputStream().use { it.copyTo(zipOut) }
                    zipOut.closeEntry()
                }
            }
            // Saved QR images
            qrDir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    zipOut.putNextEntry(ZipEntry("qrs/${file.name}"))
                    file.inputStream().use { it.copyTo(zipOut) }
                    zipOut.closeEntry()
                }
            }
        }
        return if (backupFile.exists() && backupFile.length() > 0) backupFile else null
    }
}
