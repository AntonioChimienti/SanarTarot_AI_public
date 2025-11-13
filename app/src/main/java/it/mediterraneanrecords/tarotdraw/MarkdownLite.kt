package it.mediterraneanrecords.tarotdraw

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle

/* =========================================================
   MarkdownLite — renderer semplice per .md negli assets
   Supporta **bold**, *italic* e [link](url) (cliccabili)
   ========================================================= */

@Composable
fun MarkdownLite(text: String) {
    val ctx = LocalContext.current
    val annotated = parseInline(text)  // ← resta una funzione NON @Composable

    // Render con link cliccabili
    ClickableText(
        text = annotated,
        onClick = { offset ->
            annotated.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()
                ?.let { ann ->
                    runCatching {
                        ctx.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(ann.item))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                }
        }
    )
}

/* ---------------------------------------------------------
   parseInline(): converte markdown basilare in AnnotatedString
   (nessuna chiamata @Composable qui dentro)
   --------------------------------------------------------- */
private fun parseInline(md: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < md.length) {
            when {
                // **grassetto**
                md.startsWith("**", i) -> {
                    val end = md.indexOf("**", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(md.substring(i + 2, end))
                        }
                        i = end + 2
                    } else {
                        append(md[i]); i++
                    }
                }

                // *corsivo*
                md.startsWith("*", i) -> {
                    val end = md.indexOf("*", i + 1)
                    if (end != -1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(md.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        append(md[i]); i++
                    }
                }

                // [label](url)
                md.startsWith("[", i) -> {
                    val closeLabel = md.indexOf("]", i + 1)
                    val openUrl = if (closeLabel != -1) md.indexOf("(", closeLabel + 1) else -1
                    val closeUrl = if (openUrl != -1) md.indexOf(")", openUrl + 1) else -1

                    if (closeLabel != -1 && openUrl != -1 && closeUrl != -1) {
                        val label = md.substring(i + 1, closeLabel)
                        val url = md.substring(openUrl + 1, closeUrl)

                        val start = length
                        append(label)

                        // sottolineo visivamente il link (niente MaterialTheme qui)
                        addStyle(
                            style = SpanStyle(textDecoration = TextDecoration.Underline),
                            start = start,
                            end = length
                        )
                        // annotazione che ClickableText leggerà
                        addStringAnnotation(
                            tag = "URL",
                            annotation = url,
                            start = start,
                            end = length
                        )

                        i = closeUrl + 1
                    } else {
                        append(md[i]); i++
                    }
                }

                else -> {
                    append(md[i]); i++
                }
            }
        }
    }
}