// FILE: app/src/main/java/it/mediterraneanrecords/tarotdraw/MoodEngine.kt
package it.mediterraneanrecords.tarotdraw

import android.content.Context
import it.mediterraneanrecords.tarotdraw.synonyms.DatamuseProvider
import it.mediterraneanrecords.tarotdraw.synonyms.LocalSynonymProvider
import it.mediterraneanrecords.tarotdraw.synonyms.OpenAIProvider
import it.mediterraneanrecords.tarotdraw.synonyms.SynonymCache
import java.text.Normalizer
import kotlin.math.max
import kotlin.math.min

/**
 * Analisi "locale" dello stato d’animo:
 * - normalizza testo (lowercase, accenti rimossi)
 * - gestisce negazioni (non/mai/senza/…), prefisso "in-" (infelice→negazione di felice)
 * - gestisce intensificatori (molto, davvero, pochissimo…) come fattori di scala
 * - lessico interno + opzionale override via assets JSON (IT/EN)
 *
 * Output: LlmBoosts con valori centrati su 1.0 (0..2).
 */
object MoodEngine {

    // -------------------- SINONIMI & RADICI (TOP-LEVEL) --------------------

    // Sinonimi/varianti → lemma base usato nel lessico
    private val SYNONYM_TO_BASE = mapOf(
        // allegria/contento → felice
        "allegro" to "felice",
        "allegra" to "felice",
        "allegri" to "felice",
        "allegre" to "felice",
        "allegrezza" to "felice",
        "contento" to "felice",
        "contenta" to "felice",
        "contenti" to "felice",
        "contente" to "felice",
        "lieto" to "felice",
        "lieta" to "felice",
        "lieti" to "felice",
        "liete" to "felice",
        "felicissimo" to "felice",
        "felicissima" to "felice",
        "felicissimi" to "felice",
        "felicissime" to "felice",

        // entusiasmo/ottimismo → gioia
        "entusiasta" to "gioia",
        "entusiaste" to "gioia",
        "entusiasti" to "gioia",
        "entusiasmo" to "gioia",
        "euforico" to "gioia",
        "euforica" to "gioia",
        "euforici" to "gioia",
        "ottimista" to "gioia",
        "ottimiste" to "gioia",
        "ottimisti" to "gioia",
        "ottimismo" to "gioia",

        // fiducia/speranza → sereno / speranza
        "fiducioso" to "sereno",
        "fiduciosa" to "sereno",
        "fiduciosi" to "sereno",
        "fiduciose" to "sereno",
        "speranzoso" to "speranza",
        "speranzosa" to "speranza",
        "speranzosi" to "speranza",

        // gratitudine/soddisfazione → gratitudine / sereno
        "grato" to "gratitudine",
        "grata" to "gratitudine",
        "grati" to "gratitudine",
        "grate" to "gratitudine",
        "riconoscente" to "gratitudine",
        "riconoscenti" to "gratitudine",
        "soddisfatto" to "sereno",
        "soddisfatta" to "sereno",
        "soddisfatti" to "sereno",
        "soddisfatte" to "sereno",

        // calma/tranquillità → calmo
        "tranquillo" to "calmo",
        "tranquilla" to "calmo",
        "tranquilli" to "calmo",
        "tranquille" to "calmo",
        "rilassato" to "calmo",
        "rilassata" to "calmo",
        "rilassati" to "calmo"
    )

    // Radici comuni che riconduciamo a un lemma (match su prefisso)
    private val ROOT_TO_BASE = listOf(
        "entusiast" to "gioia",
        "ottimist" to "gioia",
        "eufor" to "gioia",
        "fiducios" to "sereno",
        "riconoscen" to "gratitudine",
        "soddisf" to "sereno",
        "tranquill" to "calmo",
        "rilassat" to "calmo",
        "allegri" to "felice",
        "liet" to "felice"
    )

    // -------------------- API PUBBLICHE --------------------

    /** Analizza il testo usando il contesto (per caricare i JSON, se presenti). */
    fun analyzeMoodLocally(text: String, ctx: Context? = null): LlmBoosts {
        if (text.isBlank()) return neutral()

        // 1) prepara vocabolario (cache in RAM)
        ensureLexiconsLoaded(ctx)

        // 2) tokenizza (con sinonimi/lemmi)
        val tokens = tokenize(text)

        var majors = 1f
        var wands = 1f
        var cups = 1f
        var swords = 1f
        var pents = 1f

        // intensificatore corrente (si resetta dopo aver applicato a una parola "portante")
        var currentIntensity = 1f

        fun isNegated(index: Int): Boolean {
            val from = max(0, index - 2)
            for (i in from until index) if (tokens[i] in NEGATIONS) return true
            return false
        }

        tokens.forEachIndexed { i, raw ->
            var t = raw

            // intensificatori (molto / poco / davvero…)
            INTENSIFIERS[t]?.let { factor ->
                currentIntensity *= factor
                return@forEachIndexed
            }

            // prefisso "in-" → possibile negazione di base positiva (infelice, insicuro…)
            var negDueToPrefix = false
            if (t.startsWith("in") && t.length > 3) {
                val base = t.removePrefix("in")
                if (base in POSITIVE_BASES) {
                    t = base
                    negDueToPrefix = true
                }
            }

            val neg = isNegated(i) || negDueToPrefix || t.startsWith("non_") || t.startsWith("NON_")
            val key = t.removePrefix("non_").removePrefix("NON_")

            // cerca prima nel lessico da assets, poi in quello interno
            val eff = (lexiconFromAssets[key] ?: LEXICON_INTERNAL[key])
            if (eff != null) {
                val sign = if (neg) -1f else 1f
                val k = currentIntensity
                majors += eff.majors * sign * k
                wands += eff.wands * sign * k
                cups += eff.cups * sign * k
                swords += eff.swords * sign * k
                pents += eff.pents * sign * k
                currentIntensity = 1f // reset dopo l’applicazione
            }
        }

        return LlmBoosts(
            majors = majors.clamp(),
            wands = wands.clamp(),
            cups = cups.clamp(),
            swords = swords.clamp(),
            pentacles = pents.clamp(),
            specificMajors = emptySet(),
            specificMajorsBoost = 0f
        )
    }

    /** Merge tra boost locali e boost AI. */
    fun merge(local: LlmBoosts, llm: LlmBoosts?): LlmBoosts {
        if (llm == null) return local
        fun avg(a: Float, b: Float) = ((a + b) / 2f).clamp()
        return LlmBoosts(
            majors = avg(local.majors, llm.majors),
            wands = avg(local.wands, llm.wands),
            cups = avg(local.cups, llm.cups),
            swords = avg(local.swords, llm.swords),
            pentacles = avg(local.pentacles, llm.pentacles),
            specificMajors = (local.specificMajors + llm.specificMajors),
            specificMajorsBoost = max(local.specificMajorsBoost, llm.specificMajorsBoost)
        )
    }

    // -------------------- LESSICO & UTILS --------------------

    private fun neutral() = LlmBoosts(1f, 1f, 1f, 1f, 1f, emptySet(), 0f)

    private fun Float.clamp(lo: Float = 0f, hi: Float = 2f) = min(max(this, lo), hi)

    /** Tokenizza + normalizza + mappa sinonimi/lemmi. */
    private fun tokenize(text: String): List<String> {
        val s = Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
            .replace("’", "'")
            .replace(Regex("[^a-z0-9_' ]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        return s.split(' ')
            .filter { it.isNotBlank() }
            .map { it.trim('\'') }
            .map { token ->
                // prova mappa sinonimi → lemma
                SYNONYM_TO_BASE[token] ?: run {
                    // poi prova match su prefissi → lemma
                    val root = ROOT_TO_BASE.firstOrNull { (prefix, _) -> token.startsWith(prefix) }
                    root?.second ?: token
                }
            }
    }

    // negazioni “vicine”
    private val NEGATIONS =
        setOf("non", "mai", "senza", "mica", "neanche", "nessuno", "niente", "affatto")

    // intensificatori → moltiplicatori (puoi ampliare)
    private val INTENSIFIERS = mapOf(
        "molto" to 1.5f,
        "davvero" to 1.4f,
        "tantissimo" to 1.8f,
        "poco" to 0.6f,
        "pochissimo" to 0.4f
    )

    // basi “positive” per il prefisso in-
    private val POSITIVE_BASES = setOf("felice", "sicuro", "sereno", "calmo", "fiducia", "pace")

    // effetto elementare
    private data class Eff(
        val majors: Float = 0f,
        val wands: Float = 0f,
        val cups: Float = 0f,
        val swords: Float = 0f,
        val pents: Float = 0f,
    )

    // lessico interno “di partenza”
    private val LEXICON_INTERNAL: Map<String, Eff> = mapOf(
        // EMOZIONI +
        "felice" to Eff(cups = +0.25f, swords = -0.20f, majors = +0.05f),
        "gioia" to Eff(cups = +0.22f, swords = -0.15f),
        "amore" to Eff(cups = +0.25f, majors = +0.05f),
        "sereno" to Eff(cups = +0.18f, swords = -0.18f),
        "calmo" to Eff(cups = +0.15f, swords = -0.20f),
        "speranza" to Eff(cups = +0.18f, majors = +0.05f),
        "gratitudine" to Eff(cups = +0.20f, swords = -0.10f),

        // EMOZIONI -
        "triste" to Eff(cups = -0.22f, swords = +0.25f),
        "ansia" to Eff(swords = +0.30f, cups = -0.12f),
        "paura" to Eff(swords = +0.28f, majors = +0.05f),
        "rabbia" to Eff(wands = +0.25f, swords = +0.10f),
        "gelosia" to Eff(cups = -0.18f, swords = +0.15f),
        "solitudine" to Eff(cups = -0.18f, swords = +0.12f),

        // ENERGIA/AZIONE
        "determinato" to Eff(wands = +0.22f, swords = -0.05f),
        "confuso" to Eff(swords = +0.20f),
        "stanco" to Eff(wands = -0.18f, pents = -0.05f),

        // MATERIA/LAVORO
        "lavoro" to Eff(pents = +0.25f),
        "soldi" to Eff(pents = +0.25f),
        "stabilita" to Eff(pents = +0.18f, swords = -0.08f),

        // TRASFORMAZIONE
        "cambiamento" to Eff(majors = +0.25f),
        "trasformazione" to Eff(majors = +0.28f),
        "destino" to Eff(majors = +0.22f)
    )

    // ---------- Supporto a lessico esterno via assets JSON ----------

    // Verrà popolato la prima volta che chiami analyzeMoodLocally con un ctx
    private var lexiconFromAssets: Map<String, Eff> = emptyMap()
    private var loaded = false

    private fun ensureLexiconsLoaded(ctx: Context?) {
        if (loaded) return
        if (ctx == null) return // senza contesto usiamo solo l'interno
        val langIsEn = currentLangTag().startsWith("en", true)
        val file = if (langIsEn) "mood_lexicon_en.json" else "mood_lexicon_it.json"
        lexiconFromAssets = runCatching {
            val txt = ctx.assets.open(file).bufferedReader().use { it.readText() }
            parseLexiconJson(txt)
        }.getOrElse { emptyMap() }
        loaded = true
    }

    // mini parser JSON (formato semplice)
    private fun parseLexiconJson(raw: String): Map<String, Eff> {
        val map = mutableMapOf<String, Eff>()
        val entryRx = Regex(
            """"([^"]+)"\s*:\s*\{\s*"maj"\s*:\s*([-\d.]+)\s*,\s*"w"\s*:\s*([-\d.]+)\s*,\s*"c"\s*:\s*([-\d.]+)\s*,\s*"s"\s*:\s*([-\d.]+)\s*,\s*"p"\s*:\s*([-\d.]+)\s*\}"""
        )
        entryRx.findAll(raw).forEach {
            val k = it.groupValues[1].lowercase()
            val eff = Eff(
                majors = it.groupValues[2].toFloat(),
                wands = it.groupValues[3].toFloat(),
                cups = it.groupValues[4].toFloat(),
                swords = it.groupValues[5].toFloat(),
                pents = it.groupValues[6].toFloat()
            )
            map[k] = eff
        }
        return map
    }

    // lingua corrente dell’app (già usata altrove nel progetto)
    private fun currentLangTag(): String {
        val locales = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales()
        return if (locales.isEmpty)
            java.util.Locale.getDefault().toLanguageTag()
        else
            (0 until locales.size()).joinToString(",") { locales[it]?.toLanguageTag() ?: "" }
    }


    // === Canonicalizzazione: trasforma parole sconosciute in lemmi noti ===
    suspend fun canonicalizeMoodText(
        text: String,
        ctx: Context,
        langTag: String,
        useDatamuse: Boolean = true,
        useOpenAI: Boolean = false,
        openAiKey: String = "",
    ): String {
        if (text.isBlank()) return text
        ensureLexiconsLoaded(ctx) // per avere le basi note

        // 1) set di basi note dal lessico interno + da assets (tutto lower-case)
        val knownBases: Set<String> = buildSet {
            addAll(LEXICON_INTERNAL.keys)
            addAll(lexiconFromAssets.keys)
        }

        // util: mappa "per prefisso" verso una base (fallback soft)
        fun rootToBaseByPrefix(token: String): String? {
            val t = token.lowercase()
            val hit = ROOT_TO_BASE.firstOrNull { (prefix, _) -> t.startsWith(prefix) }
            return hit?.second
        }

        // 2) helper locali/remoti
        suspend fun mapLocal(token: String): String? {
            // prova mappa sinonimi locale
            val cands = LocalSynonymProvider.lookup(token, langTag)
            // prendi il primo che appartiene alle basi note, oppure prova il fallback per prefissi
            return cands.firstOrNull { it in knownBases } ?: cands.firstOrNull()
                ?.let { rootToBaseByPrefix(it) }
        }

        suspend fun mapRemote(token: String): String? {
            // Cache
            SynonymCache.get(ctx, token)?.let { return it }

            // Datamuse (gratuito)
            if (useDatamuse) {
                val cands = DatamuseProvider.lookup(token, langTag)
                // 1: se uno è già una base nota → usa quello
                cands.firstOrNull { it in knownBases }?.let {
                    SynonymCache.put(ctx, token, it)
                    return it
                }
                // 2: prova a ricondurre per prefisso (es. "spaventato" → "paura")
                cands.firstNotNullOfOrNull { rootToBaseByPrefix(it) }?.let {
                    SynonymCache.put(ctx, token, it)
                    return it
                }
                // 3: prova la tabella locale come secondo pass
                cands.firstNotNullOfOrNull { mapLocal(it) }?.let {
                    SynonymCache.put(ctx, token, it)
                    return it
                }
            }

            // OpenAI (opzionale)
            if (useOpenAI && openAiKey.isNotBlank()) {
                val base = OpenAIProvider.classifyToBase(token, langTag, openAiKey)
                if (base != null) {
                    SynonymCache.put(ctx, token, base)
                    return base
                }
            }
            return null
        }

        // 3) tokenizza e sostituisci parola-per-parola
        val tokens = tokenize(text)
        val out = ArrayList<String>(tokens.size)

        for (raw in tokens) {
            // se già è una base nota → lascia intatto
            if (raw in knownBases) {
                out += raw; continue
            }

            // prova locale
            val loc = mapLocal(raw)
            if (loc != null && loc in knownBases) {
                out += loc; continue
            }

            // prova remoto (cache → datamuse → openai)
            val rem = mapRemote(raw)
            if (rem != null && rem in knownBases) {
                out += rem; continue
            }

            // ultimo tentativo: prefisso sul token stesso
            val root = rootToBaseByPrefix(raw)
            if (root != null) {
                out += root; continue
            }

            // fallback: lascia com'è
            out += raw
        }

        return out.joinToString(" ")
    }// Verifica robusta: almeno un token (dopo canonicalizzazione) è una "base" nota?

    suspend fun isKnownMoodText(
        text: String,
        ctx: Context,
        langTag: String,
        useDatamuse: Boolean = true,
        useOpenAI: Boolean = false,
        openAiKey: String = "",
    ): Boolean {
        if (text.isBlank()) return false

        // Canonicalizza usando l’intera pipeline: Locale → Cache → DataMuse → OpenAI/Gemini (se attivo)
        val canon = canonicalizeMoodText(
            text = text,
            ctx = ctx,
            langTag = langTag,
            useDatamuse = useDatamuse,
            useOpenAI = useOpenAI,
            openAiKey = openAiKey
        )

        // Confronta coi lemmi base noti (lessico interno + da assets)
        ensureLexiconsLoaded(ctx)
        val known = (LEXICON_INTERNAL.keys + lexiconFromAssets.keys).toSet()

        val tokens = tokenize(canon)
        return tokens.any { it in known }
    }// Verifica se nel testo c'è ALMENO una parola riconoscibile

    // (lessico interno/asset, sinonimi locali, Datamuse e opzionale OpenAI)
    suspend fun hasAnyKnownWord(
        text: String,
        ctx: Context,
        langTag: String,
        useDatamuse: Boolean = true,
        useOpenAI: Boolean = false,
        openAiKey: String = "",
    ): Boolean {
        if (text.isBlank()) return false
        ensureLexiconsLoaded(ctx)

        // insiemi di lemmi "noti"
        val knownBases: Set<String> = buildSet {
            addAll(LEXICON_INTERNAL.keys)
            addAll(lexiconFromAssets.keys)
        }

        // piccolo helper per ricondurre per prefisso (es. "spaventato" -> "paura")
        fun rootToBaseByPrefix(token: String): String? {
            val t = token.lowercase()
            val hit = ROOT_TO_BASE.firstOrNull { (prefix, _) -> t.startsWith(prefix) }
            return hit?.second
        }

        // scansiona i token del testo
        for (raw in tokenize(text)) {
            val token = raw.lowercase()

            // 1) già una base nota?
            if (token in knownBases) return true

            // 2) sinonimi locali
            val loc = LocalSynonymProvider.lookup(token, langTag)
            if (loc.any { it in knownBases }) return true
            if (loc.mapNotNull { rootToBaseByPrefix(it) }.any { it in knownBases }) return true

            // 3) Datamuse (se abilitato)
            if (useDatamuse) {
                val dm = DatamuseProvider.lookup(token, langTag)
                if (dm.any { it in knownBases }) return true
                if (dm.mapNotNull { rootToBaseByPrefix(it) }.any { it in knownBases }) return true
            }

            // 4) OpenAI/Gemini (se abilitato e con chiave)
            if (useOpenAI && openAiKey.isNotBlank()) {
                val base = OpenAIProvider.classifyToBase(token, langTag, openAiKey)
                if (base != null && base in knownBases) return true
            }
        }
        return false
    }

    // ============================================================
    // Heuristica "suggeriti" + pesi LLM
    // ============================================================
    @Volatile
    private var _suggestedMajors: Set<Int> = emptySet()
    val suggestedMajors: Set<Int> get() = _suggestedMajors

    fun updateSuggestedMajorsFromText(text: String, langTag: String) {
        val t = text.lowercase().trim()
        val isEn = langTag.startsWith("en", ignoreCase = true)
        val hits = mutableSetOf<Int>()
        fun add(vararg ids: Int) {
            ids.forEach { hits.add(it) }
        }

        if (!isEn) {
            if (t.contains("felice") || t.contains("gioia") || t.contains("speranza")) add(
                17,
                19,
                14
            )
            if (t.contains("triste") || t.contains("giù") || t.contains("solo")) add(12, 18)
            if (t.contains("ansios") || t.contains("paura") || t.contains("preoccupat")) add(18, 9)
            if (t.contains("arrabbiat") || t.contains("rabbia")) add(16, 15)
            if (t.contains("amore") || t.contains("romant")) add(6, 3)
            if (t.contains("confus") || t.contains("smarrit")) add(0, 18)
        } else {
            if (t.contains("happy") || t.contains("joy") || t.contains("hope")) add(17, 19, 14)
            if (t.contains("sad") || t.contains("down") || t.contains("lonely")) add(12, 18)
            if (t.contains("anxious") || t.contains("fear") || t.contains("worry")) add(18, 9)
            if (t.contains("angry") || t.contains("rage")) add(16, 15)
            if (t.contains("love") || t.contains("romance")) add(6, 3)
            if (t.contains("confused") || t.contains("lost")) add(0, 18)
        }

        _suggestedMajors = hits
    }

    /** Peso extra per una carta dato il “mood” e i suggeriti */
    fun extraWeightForCard(card: TarotCard, mood: LlmBoosts?): Float {
        var w = 0f
        if (card.index in suggestedMajors) w += 0.15f
        mood?.let {
            val suitDelta = when {
                card.index < 22 -> it.majors - 1f
                card.name.contains("Bastoni", true) -> it.wands - 1f
                card.name.contains("Coppe", true) -> it.cups - 1f
                card.name.contains("Spade", true) -> it.swords - 1f
                else -> it.pentacles - 1f
            }
            w += suitDelta.coerceAtLeast(0f)
            if (card.index in it.specificMajors) w += (it.specificMajorsBoost - 1f).coerceAtLeast(0f)
        }
        return w
    }

    /** Prompt per OpenAI/Gemini */
    fun buildLlmPrompt(moodText: String): String {
        val schema = """
          {
            "majors": number,
            "wands": number,
            "cups": number,
            "swords": number,
            "pentacles": number,
            "specificMajors": [int],
            "specificMajorsBoost": number
          }
        """.trimIndent()

        return """
          Analizza la nota emotiva dell’utente e restituisci SOLO JSON conforme a:
          $schema

          Nota emotiva:
          "$moodText"
        """.trimIndent()
    }
}