package com.local.paybio.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import com.local.paybio.data.PaymentMethod
import java.io.File
import java.io.FileOutputStream

/** Fast-Share: hands formatted payment data + QR image to the OS share sheet. */
object ShareUtil {

    fun buildText(method: PaymentMethod): String = buildString {
        appendLine("💸 ${method.platformName} (${method.country})")
        appendLine("Titular: ${method.holderName}")
        appendLine("${if (method.type == "Blockchain") "Dirección" else "Cuenta"}: ${method.accountNumber}")
        append("— vía PayBio (Smart Offline Ledger)")
    }

    fun sharePaymentMethod(context: Context, method: PaymentMethod) {
        val text = buildText(method)
        val qrUri = QrGenerator.generate(method.accountNumber)
            ?.let { saveBitmapToCache(context, it, "qr_share_${method.id}.png") }

        val intent = Intent(Intent.ACTION_SEND).apply {
            if (qrUri != null) {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, qrUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                type = "text/plain"
            }
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(
            Intent.createChooser(intent, "Compartir cobro")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun shareFile(context: Context, file: File, mime: String, title: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(intent, title).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun saveBitmapToCache(context: Context, bitmap: Bitmap, name: String): android.net.Uri {
        val file = File(context.cacheDir, name)
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
