package it.mediterraneanrecords.tarotdraw

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlin.random.Random

/* ===========================
   Selettore mazzo con preview
   =========================== */

@Composable
fun DeckPickerDialog(
    current: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val ctx = LocalContext.current
    var tmp by remember { mutableStateOf(current) }
    var fullScreenCard: Int? by remember { mutableStateOf(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Scegli il mazzo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DeckOptionRow(
                    label = "Base / Standard",
                    value = "base",
                    selected = tmp == "base",
                    sampleRes = sampleCardId(ctx, "base"),
                    onClick = { tmp = "base" },
                    onPreviewTap = { fullScreenCard = it }
                )
                DeckOptionRow(
                    label = "Sanar Lux Aeterna",
                    value = "lux",
                    selected = tmp == "lux",
                    sampleRes = sampleCardId(ctx, "lux"),
                    onClick = { tmp = "lux" },
                    onPreviewTap = { fullScreenCard = it }
                )
                DeckOptionRow(
                    label = "Celestia Aurora",
                    value = "cel",
                    selected = tmp == "cel",
                    sampleRes = sampleCardId(ctx, "cel"),
                    onClick = { tmp = "cel" },
                    onPreviewTap = { fullScreenCard = it }
                )
                DeckOptionRow(
                    label = "Arcana Nova",
                    value = "arc",               // <-- usa "arc" come deciso
                    selected = tmp == "arc",
                    sampleRes = sampleCardId(ctx, "arc"),
                    onClick = { tmp = "arc" },
                    onPreviewTap = { fullScreenCard = it }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSelect(tmp) }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        }
    )

    // Carta a tutto schermo con fade + zoom
    fullScreenCard?.let { resId ->
        FullscreenCardDialog(imageRes = resId) { fullScreenCard = null }
    }
}

/* -------------------------- */
/*  Riga opzione con preview  */
/* -------------------------- */

@Composable
private fun DeckOptionRow(
    label: String,
    value: String,
    selected: Boolean,
    @DrawableRes sampleRes: Int,
    onClick: () -> Unit,
    onPreviewTap: (Int) -> Unit,
) {
    val thumbShape = RoundedCornerShape(10.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(8.dp))
        Text(text = label, modifier = Modifier.weight(1f))

        if (sampleRes != 0) {
            Image(
                painter = painterResource(sampleRes),
                contentDescription = "Anteprima carta",
                modifier = Modifier
                    .size(56.dp)
                    .clip(thumbShape)
                    .border(1.dp, Color.White.copy(alpha = .25f), thumbShape)
                    .clickable { onPreviewTap(sampleRes) }
            )
        }
    }
}

/* --------------------------------- */
/*  Dialog full-screen con animazioni */
/* --------------------------------- */

@Composable
private fun FullscreenCardDialog(
    @DrawableRes imageRes: Int,
    onDismiss: () -> Unit,
) {
    // animazioni: fade + zoom leggero
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "alpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.96f,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "scale"
    )

    // parte l’animazione appena il dialog si mostra
    LaunchedEffect(Unit) { visible = true }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = "Carta",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .graphicsLayer(alpha = alpha, scaleX = scale, scaleY = scale)
            )
        }
    }
}

/* ===================== */
/*  Helper: sample carta */
/* ===================== */

@DrawableRes
private fun sampleCardId(ctx: Context, deck: String): Int {
    val prefix = when (deck.lowercase()) {
        "base" -> "card"
        "lux" -> "lux_card"
        "cel" -> "cel_card"
        "arc" -> "arc_card"   // <-- Arcana Nova
        else -> "card"
    }

    // Prova 3 candidati sicuri presenti in tutti i mazzi
    val candidates = listOf(
        "${prefix}_00_il_matto",
        "${prefix}_01_il_bagatto",
        "${prefix}_10_la_ruota_della_fortuna"
    )

    // restituisce il primo esistente
    for (name in candidates) {
        val id = ctx.resources.getIdentifier(name, "drawable", ctx.packageName)
        if (id != 0) return id
    }

    // fallback: tenta un maggiore a caso (00..21) per dare varietà
    val n = Random.nextInt(0, 22).toString().padStart(2, '0')
    val alt = ctx.resources.getIdentifier("${prefix}_${n}_il_matto", "drawable", ctx.packageName)
    return if (alt != 0) alt else 0
}