package com.ts.summarylog.util

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object NotificationImageHelper {

    fun extractAndSaveImage(context: Context, extras: Bundle): String? {
        val bitmap = when {
            extras.containsKey(NotificationCompat.EXTRA_PICTURE) -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    extras.getParcelable(NotificationCompat.EXTRA_PICTURE, Bitmap::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    extras.getParcelable(NotificationCompat.EXTRA_PICTURE) as? Bitmap
                }
            }
            extras.containsKey(NotificationCompat.EXTRA_LARGE_ICON_BIG) -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    extras.getParcelable(NotificationCompat.EXTRA_LARGE_ICON_BIG, Bitmap::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    extras.getParcelable(NotificationCompat.EXTRA_LARGE_ICON_BIG) as? Bitmap
                }
            }
            else -> null
        } ?: return null

        return try {
            val directory = File(context.filesDir, "notification_images").apply { if (!exists()) mkdirs() }
            val file = File(directory, "${UUID.randomUUID()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}
