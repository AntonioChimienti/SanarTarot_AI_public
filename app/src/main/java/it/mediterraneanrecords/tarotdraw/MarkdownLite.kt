package it.mediterraneanrecords.tarotdraw

import android.content.Intent
import android.net.Uri
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon

/* =========================================================
   MarkdownFull — renderer completo con Markwon
   Supporta titoli (#), bold (**), italic (*),
   liste, link cliccabili e layout ben formattato.
   ========================================================= */

@Composable
fun MarkdownFull(
    text: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Markwon viene ricordato e creato una sola volta
    val markwon = remember { Markwon.create(context) }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            TextView(ctx).apply {
                // Permette ai link di essere cliccati
                movementMethod = LinkMovementMethod.getInstance()
                textSize = 17f        // regola come preferisci
                setLineSpacing(0f, 1.2f)
            }
        },
        update = { tv ->
            // Markwon renderizza il Markdown completo
            markwon.setMarkdown(tv, text)
        }
    )
}

/* =========================================================
   MarkdownFull — versione leggera con solo bold/corsivo/link
   (puoi continuare a usarla se ti serve in qualche punto)
   ========================================================= */

@Composable
fun MarkdownLite(text: String) {
    val ctx = LocalContext.current
    val annotated = parseInline(text)  // ← funzione NON @Composable

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

                        // sottolineo visivamente il link
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