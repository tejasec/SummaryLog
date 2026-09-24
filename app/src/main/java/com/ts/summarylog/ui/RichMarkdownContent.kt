package com.ts.summarylog.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

sealed interface MarkdownSegment {
    data class Text(val text: String) : MarkdownSegment
    data class Code(val code: String, val language: String) : MarkdownSegment
}

@Composable
fun RichMarkdownContent(
    content: String,
    textColor: Color,
    isStreaming: Boolean,
    modifier: Modifier = Modifier
) {
    val segments = remember(content) {
        val parts = mutableListOf<MarkdownSegment>()
        val regex = Regex("```(\\w*)\\n?([\\s\\S]*?)```")
        var currentIndex = 0

        for (match in regex.findAll(content)) {
            val textBefore = content.substring(currentIndex, match.range.first)
            if (textBefore.isNotBlank()) {
                parts.add(MarkdownSegment.Text(textBefore))
            }
            val lang = match.groupValues[1]
            val code = match.groupValues[2]
            parts.add(MarkdownSegment.Code(code = code, language = lang))
            currentIndex = match.range.last + 1
        }

        if (currentIndex < content.length) {
            parts.add(MarkdownSegment.Text(content.substring(currentIndex)))
        }
        parts
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        segments.forEachIndexed { index, segment ->
            when (segment) {
                is MarkdownSegment.Text -> {
                    Row {
                        MarkdownText(markdown = segment.text, textColor = textColor)
                        if (isStreaming && index == segments.lastIndex) {
                            BlinkingCursor()
                        }
                    }
                }
                is MarkdownSegment.Code -> {
                    CodeBlockView(code = segment.code, language = segment.language)
                }
            }
        }

        if (isStreaming && segments.isEmpty()) {
            BlinkingCursor()
        }
    }
}
