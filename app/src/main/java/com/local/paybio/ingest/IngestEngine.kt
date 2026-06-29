package com.local.paybio.ingest

import android.content.Context
import android.net.Uri
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class IngestOutput(
    val text: String,
    val barcodes: List<String>
)

/**
 * On-device extraction (Module 1): runs ML Kit Text Recognition + Barcode
 * Scanning over a picked image. Fully local; the bundled Latin model ships
 * inside the APK so no internet is required.
 */
object IngestEngine {

    suspend fun fromImage(context: Context, uri: Uri): IngestOutput = withContext(Dispatchers.IO) {
        val image = InputImage.fromFilePath(context, uri)

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val scanner = BarcodeScanning.getClient()

        val text = runCatching { Tasks.await(recognizer.process(image)).text }.getOrDefault("")
        val barcodes = runCatching {
            Tasks.await(scanner.process(image)).mapNotNull { it.rawValue }
        }.getOrDefault(emptyList())

        recognizer.close()
        scanner.close()
        IngestOutput(text = text, barcodes = barcodes)
    }
}
