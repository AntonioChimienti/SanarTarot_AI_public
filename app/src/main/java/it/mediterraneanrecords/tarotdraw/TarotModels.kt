package it.mediterraneanrecords.tarotdraw

import android.content.Context
import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.util.Locale

/* ============================================================
   DATA CLASSES
   ============================================================ */

@Parcelize
data class TarotCard(
    val index: Int,
    val name: String,
    val code: String? = null,
    val it: String? = null,
    val en: String? = null,
) : Parcelable

@Parcelize
data class DrawnCard(
    val card: TarotCard,
    val reversed: Boolean,
) : Parcelable


/* ============================================================
   MAZZO COMPLETO — 78 CARTE
   ============================================================ */

object TarotDeck {
    val cards: List<TarotCard> = listOf(
        // === ARCANI MAGGIORI (0–21) ===
        TarotCard(0, "Il Matto"),
        TarotCard(1, "Il Bagatto"),
        TarotCard(2, "La Papessa"),
        TarotCard(3, "L’Imperatrice"),
        TarotCard(4, "L’Imperatore"),
        TarotCard(5, "Il Papa"),
        TarotCard(6, "Gli Amanti"),
        TarotCard(7, "Il Carro"),
        TarotCard(8, "La Giustizia"),
        TarotCard(9, "L’Eremita"),
        TarotCard(10, "La Ruota della Fortuna"),
        TarotCard(11, "La Forza"),
        TarotCard(12, "L’Appeso"),
        TarotCard(13, "La Morte"),
        TarotCard(14, "La Temperanza"),
        TarotCard(15, "Il Diavolo"),
        TarotCard(16, "La Torre"),
        TarotCard(17, "Le Stelle"),
        TarotCard(18, "La Luna"),
        TarotCard(19, "Il Sole"),
        TarotCard(20, "Il Giudizio"),
        TarotCard(21, "Il Mondo"),

        // === ARCANI MINORI — BASTONI ===
        TarotCard(22, "Asso di Bastoni"),
        TarotCard(23, "Due di Bastoni"),
        TarotCard(24, "Tre di Bastoni"),
        TarotCard(25, "Quattro di Bastoni"),
        TarotCard(26, "Cinque di Bastoni"),
        TarotCard(27, "Sei di Bastoni"),
        TarotCard(28, "Sette di Bastoni"),
        TarotCard(29, "Otto di Bastoni"),
        TarotCard(30, "Nove di Bastoni"),
        TarotCard(31, "Dieci di Bastoni"),
        TarotCard(32, "Fante di Bastoni"),
        TarotCard(33, "Cavaliere di Bastoni"),
        TarotCard(34, "Regina di Bastoni"),
        TarotCard(35, "Re di Bastoni"),

        // === ARCANI MINORI — COPPE ===
        TarotCard(36, "Asso di Coppe"),
        TarotCard(37, "Due di Coppe"),
        TarotCard(38, "Tre di Coppe"),
        TarotCard(39, "Quattro di Coppe"),
        TarotCard(40, "Cinque di Coppe"),
        TarotCard(41, "Sei di Coppe"),
        TarotCard(42, "Sette di Coppe"),
        TarotCard(43, "Otto di Coppe"),
        TarotCard(44, "Nove di Coppe"),
        TarotCard(45, "Dieci di Coppe"),
        TarotCard(46, "Fante di Coppe"),
        TarotCard(47, "Cavaliere di Coppe"),
        TarotCard(48, "Regina di Coppe"),
        TarotCard(49, "Re di Coppe"),

        // === ARCANI MINORI — SPADE ===
        TarotCard(50, "Asso di Spade"),
        TarotCard(51, "Due di Spade"),
        TarotCard(52, "Tre di Spade"),
        TarotCard(53, "Quattro di Spade"),
        TarotCard(54, "Cinque di Spade"),
        TarotCard(55, "Sei di Spade"),
        TarotCard(56, "Sette di Spade"),
        TarotCard(57, "Otto di Spade"),
        TarotCard(58, "Nove di Spade"),
        TarotCard(59, "Dieci di Spade"),
        TarotCard(60, "Fante di Spade"),
        TarotCard(61, "Cavaliere di Spade"),
        TarotCard(62, "Regina di Spade"),
        TarotCard(63, "Re di Spade"),

        // === ARCANI MINORI — DENARI ===
        TarotCard(64, "Asso di Denari"),
        TarotCard(65, "Due di Denari"),
        TarotCard(66, "Tre di Denari"),
        TarotCard(67, "Quattro di Denari"),
        TarotCard(68, "Cinque di Denari"),
        TarotCard(69, "Sei di Denari"),
        TarotCard(70, "Sette di Denari"),
        TarotCard(71, "Otto di Denari"),
        TarotCard(72, "Nove di Denari"),
        TarotCard(73, "Dieci di Denari"),
        TarotCard(74, "Fante di Denari"),
        TarotCard(75, "Cavaliere di Denari"),
        TarotCard(76, "Regina di Denari"),
        TarotCard(77, "Re di Denari")
    )
}

/* ============================================================
   STRINGA → NOME RISORSA
   ============================================================ */

fun String.normalizeForRes(): String =
    lowercase(Locale.ROOT)
        .replace("à", "a").replace("è", "e").replace("é", "e")
        .replace("ì", "i").replace("ò", "o").replace("ù", "u")
        .replace("’", "").replace("'", "")
        .replace("ç", "c")
        .replace(Regex("[^a-z0-9 ]"), " ")
        .replace(Regex("\\s+"), "_")
        .trim('_')

fun String.wordsToNumerals(): String {
    var s = this.replace('_', ' ')
    s = s.replace(Regex("\\bdue\\b", RegexOption.IGNORE_CASE), "2")
        .replace(Regex("\\btre\\b", RegexOption.IGNORE_CASE), "3")
        .replace(Regex("\\bquattro\\b", RegexOption.IGNORE_CASE), "4")
        .replace(Regex("\\bcinque\\b", RegexOption.IGNORE_CASE), "5")
        .replace(Regex("\\bsei\\b", RegexOption.IGNORE_CASE), "6")
        .replace(Regex("\\bsette\\b", RegexOption.IGNORE_CASE), "7")
        .replace(Regex("\\botto\\b", RegexOption.IGNORE_CASE), "8")
        .replace(Regex("\\bnove\\b", RegexOption.IGNORE_CASE), "9")
        .replace(Regex("\\bdieci\\b", RegexOption.IGNORE_CASE), "10")
    return s.replace(' ', '_')
}

private val ROMANS = arrayOf(
    "0", "i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix",
    "x", "xi", "xii", "xiii", "xiv", "xv", "xvi", "xvii", "xviii", "xix", "xx", "xxi"
)

private fun romanForMajor(index: Int): String? =
    if (index in 0..21) ROMANS[index] else null

/* ============================================================
   DRAWABLE FINDER CON MAZZO ("deck")
   ============================================================ */

@DrawableRes
fun cardDrawableResForDeck(ctx: Context, card: TarotCard, deck: String): Int {
    val pkg = ctx.packageName
    val idx2 = card.index.toString().padStart(2, '0')
    val raw = card.name.normalizeForRes()
    val alt = raw.wordsToNumerals()
    val roman = romanForMajor(card.index)

    // Varianti base
    val baseCandidates = buildList {
        if (roman != null) {
            add("card_${idx2}_${roman}_${raw}")
            add("card_${idx2}_${roman}_${alt}")
        }
        add("card_${idx2}_${alt}")
        add("card_${idx2}_${raw}")
        add("card_${idx2}")
    }

    // Se deck non è base, aggiunge prefisso
    val deckedCandidates = if (deck.equals("base", true)) {
        baseCandidates
    } else {
        baseCandidates.map {
            when (deck.lowercase()) {
                "lux" -> "lux_${it}"
                "cel" -> "cel_${it}"
                "arc" -> "arc_${it}"
                else -> it
            }
        }
    }

    // 1) Prova con il mazzo scelto
    for (name in deckedCandidates) {
        val id = ctx.resources.getIdentifier(name, "drawable", pkg)
        if (id != 0) return id
    }

    // 2) Fallback base
    for (name in baseCandidates) {
        val id = ctx.resources.getIdentifier(name, "drawable", pkg)
        if (id != 0) return id
    }

    // 3) Carta nera
    return R.drawable.carta_nera
}

/* ============================================================
   DEBUG — TROVA CARTE MANCANTI
   ============================================================ */

fun candidateNamesForCard(card: TarotCard): List<String> {
    val idx2 = card.index.toString().padStart(2, '0')
    val raw = card.name.normalizeForRes()
    val alt = raw.wordsToNumerals()
    val roman = romanForMajor(card.index)

    val out = mutableListOf<String>()
    if (roman != null) {
        out += "card_${idx2}_${roman}_$raw"
        out += "card_${idx2}_${roman}_$alt"
    }
    out += "card_${idx2}_$alt"
    out += "card_${idx2}_$raw"
    out += "card_${idx2}"
    return out
}

fun resolveDrawableWithName(ctx: Context, card: TarotCard): Pair<Int, String?> {
    val pkg = ctx.packageName
    for (name in candidateNamesForCard(card)) {
        val id = ctx.resources.getIdentifier(name, "drawable", pkg)
        if (id != 0) return id to name
    }
    return R.drawable.carta_nera to null
}

fun buildMissingImagesReport(ctx: Context): String {
    val sb = StringBuilder()
    var missing = 0
    for (c in TarotDeck.cards) {
        val (_, matched) = resolveDrawableWithName(ctx, c)
        if (matched == null) {
            missing++
            sb.appendLine("${c.index.toString().padStart(2, '0')}  ${c.name}")
            sb.appendLine("  provati:")
            candidateNamesForCard(c).forEach { sb.appendLine("    • $it") }
            sb.appendLine()
        }
    }
    sb.insert(0, "MANCANTI: $missing / ${TarotDeck.cards.size}\n\n")
    return sb.toString()
}
/* ============================================================
   TIPI CONDIVISI PER AI / LLM (usati in ApiConfig, LimClient, MainActivity)
   ============================================================ */

enum class AiProvider { NONE, OPENAI, GEMINI }

/**
 * Pesi/boost per la logica di estrazione.
 * Valore 1.0 = neutro (nessun boost). >1 favorisce, <1 sfavorisce.
 */
@Serializable
data class LlmBoosts(
    val majors: Float = 1f,        // Arcani Maggiori
    val wands: Float = 1f,         // Bastoni
    val cups: Float = 1f,          // Coppe
    val swords: Float = 1f,        // Spade
    val pentacles: Float = 1f,     // Denari
    val specificMajors: Set<Int> = emptySet(),  // indici 0..21 favoriti
    val specificMajorsBoost: Float = 0f,         // bonus aggiuntivo per quelli
)

/**
 * Risultato standardizzato di una chiamata LLM (es. OpenAI/Gemini).
 * Se c'è un errore 'error' viene valorizzato; altrimenti 'boosts'.
 */
@Serializable
data class LlmResult(
    val boosts: LlmBoosts? = null,
    val error: String? = null,
)