package com.ts.summarylog.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

@Composable
fun MarkdownText(
    markdown: String,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val titleMediumFontSize = MaterialTheme.typography.titleMedium.fontSize
    val titleLargeFontSize = MaterialTheme.typography.titleLarge.fontSize

    val annotatedString = remember(markdown, textColor, primaryColor, titleMediumFontSize, titleLargeFontSize) {
        buildAnnotatedString {
            val lines = markdown.split("\n")
            lines.forEachIndexed { index, line ->
                val trimmed = line.trim()
                when {
                    // Headers: ### Title or ## Title
                    trimmed.startsWith("### ") -> {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = titleMediumFontSize)) {
                            append(trimmed.removePrefix("### "))
                        }
                    }
                    trimmed.startsWith("## ") -> {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = titleLargeFontSize)) {
                            append(trimmed.removePrefix("## "))
                        }
                    }
                    // Bullet points: * Item or - Item
                    trimmed.startsWith("* ") || trimmed.startsWith("- ") -> {
                        withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold)) {
                            append("• ")
                        }
                        parseInlineMarkdown(trimmed.substring(2), textColor)
                    }
                    // Regular paragraph text
                    else -> {
                        parseInlineMarkdown(line, textColor)
                    }
                }
                if (index < lines.lastIndex) {
                    append("\n")
                }
            }
        }
    }

    Text(
        text = annotatedString,
        color = textColor,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier
    )
}

/**
 * Handles inline styles: **bold**, *italic*, and `code`
 */
private fun AnnotatedString.Builder.parseInlineMarkdown(text: String, defaultColor: Color) {
    val regex = Regex("(\\*\\*.*?\\*\\*|`.*?`|\\*.*?\\*)")
    var lastIndex = 0

    for (match in regex.findAll(text)) {
        if (match.range.first > lastIndex) {
            append(text.substring(lastIndex, match.range.first))
        }

        val token = match.value
        when {
            token.startsWith("**") && token.endsWith("**") -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(token.removeSurrounding("**"))
                }
            }
            token.startsWith("`") && token.endsWith("`") -> {
                withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = defaultColor.copy(alpha = 0.12f),
                        fontWeight = FontWeight.Medium
                    )
                ) {
                    append(" ${token.removeSurrounding("`")} ")
                }
            }
            token.startsWith("*") && token.endsWith("*") -> {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(token.removeSurrounding("*"))
                }
            }
        }
        lastIndex = match.range.last + 1
    }

    if (lastIndex < text.length) {
        append(text.substring(lastIndex))
    }
}
