package com.example.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.EnumMap

object QrCodeUtil {

    /**
     * Generates a high-contrast QR code bitmap using ZXing.
     */
    fun generateQrBitmap(
        content: String,
        width: Int = 512,
        height: Int = 512
    ): Bitmap? {
        return try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
                put(EncodeHintType.MARGIN, 1)
            }
            val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints)
            val matrixWidth = bitMatrix.width
            val matrixHeight = bitMatrix.height
            val pixels = IntArray(matrixWidth * matrixHeight)
            for (y in 0 until matrixHeight) {
                val offset = y * matrixWidth
                for (x in 0 until matrixWidth) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
                }
            }
            val bitmap = Bitmap.createBitmap(matrixWidth, matrixHeight, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, matrixWidth, 0, 0, matrixWidth, matrixHeight)
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Encodes a standard Happy Paws Liberia Pet Passport QR string.
     */
    fun buildPetPayload(petId: Long, name: String, rabiesTag: String, microchipId: String): String {
        return "HAPPYPAWS:PET:$petId:$name:$rabiesTag:$microchipId"
    }

    /**
     * Parses scanned QR payload to extract the target petId.
     */
    fun extractPetIdFromPayload(payload: String): Long? {
        val trimmed = payload.trim()
        if (trimmed.startsWith("HAPPYPAWS:PET:")) {
            val parts = trimmed.split(":")
            if (parts.size >= 3) {
                return parts[2].toLongOrNull()
            }
        }
        // Direct ID support
        trimmed.toLongOrNull()?.let { return it }

        // JSON support
        val petIdMatch = Regex("""(?i)"petId"\s*:\s*(\d+)""").find(trimmed)
        if (petIdMatch != null) {
            return petIdMatch.groupValues[1].toLongOrNull()
        }

        // Tag format e.g. HP-PET-1
        val hpMatch = Regex("""(?i)HP-PET-(\d+)""").find(trimmed)
        if (hpMatch != null) {
            return hpMatch.groupValues[1].toLongOrNull()
        }

        return null
    }
}
