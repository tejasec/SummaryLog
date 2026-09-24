package com.ts.summarylog.util

import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.ts.summarylog.data.NotificationEntity
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {
    fun exportConversationToPdf(
        title: String,
        packageName: String,
        messages: List<NotificationEntity>,
        outputStream: OutputStream,
    ) {
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            isFakeBoldText = true
        }

        val subTitlePaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 12f
        }

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
        }

        val timePaint = Paint().apply {
            color = Color.GRAY
            textSize = 9f
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        var yPos = 40f

        canvas.drawText("SummaryLog Conversation Export", 36f, yPos, titlePaint)
        yPos += 20f
        canvas.drawText("Conversation: $title ($packageName)", 36f, yPos, subTitlePaint)
        yPos += 15f
        val exportedAt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Exported at: $exportedAt | Total messages: ${messages.size}", 36f, yPos, timePaint)
        yPos += 15f
        canvas.drawLine(36f, yPos, pageWidth - 36f, yPos, linePaint)
        yPos += 25f

        val sdf = SimpleDateFormat("h:mm a, MMM d", Locale.getDefault())

        for (msg in messages) {
            if (yPos > pageHeight - 60f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPos = 40f
            }

            val msgTime = sdf.format(Date(msg.timestamp))
            canvas.drawText(msgTime, 36f, yPos, timePaint)
            yPos += 14f

            val maxTextWidth = (pageWidth - 72).toFloat()
            val words = msg.text.split(" ")
            var line = ""
            for (word in words) {
                val testLine = if (line.isEmpty()) word else "$line $word"
                if (textPaint.measureText(testLine) > maxTextWidth) {
                    canvas.drawText(line, 36f, yPos, textPaint)
                    yPos += 14f
                    line = word
                } else {
                    line = testLine
                }
            }
            if (line.isNotEmpty()) {
                canvas.drawText(line, 36f, yPos, textPaint)
                yPos += 14f
            }

            yPos += 10f
            canvas.drawLine(36f, yPos, pageWidth - 36f, yPos, linePaint)
            yPos += 15f
        }

        pdfDocument.finishPage(page)

        try {
            pdfDocument.writeTo(outputStream)
        } finally {
            pdfDocument.close()
        }
    }
}
