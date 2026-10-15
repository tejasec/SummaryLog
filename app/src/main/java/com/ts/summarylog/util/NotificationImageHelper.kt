package com.ts.summarylog.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object NotificationImageHelper {

    fun extractAndSaveImage(context: Context, extras: Bundle): String? {
        val bitmap = extractBitmap(context, extras, NotificationCompat.EXTRA_PICTURE)
            ?: extractBitmap(context, extras, NotificationCompat.EXTRA_LARGE_ICON_BIG)
            ?: return null

        return try {
            val directory = File(context.filesDir, "notification_images").apply { if (!exists()) mkdirs() }
            val file = File(directory, "${UUID.randomUUID()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            file.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    private fun extractBitmap(context: Context, extras: Bundle, key: String): Bitmap? {
        if (!extras.containsKey(key)) return null
        return try {
            val parcelable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                extras.getParcelable(key, Any::class.java)
            } else {
                @Suppress("DEPRECATION")
                extras.getParcelable(key)
            }
            when (parcelable) {
                is Bitmap -> parcelable
                is Icon -> iconToBitmap(context, parcelable)
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun iconToBitmap(context: Context, icon: Icon): Bitmap? {
        return try {
            val drawable = icon.loadDrawable(context) ?: return null
            if (drawable is BitmapDrawable && drawable.bitmap != null) {
                return drawable.bitmap
            }
            val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
            val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bmp
        } catch (_: Exception) {
            null
        }
    }
}
