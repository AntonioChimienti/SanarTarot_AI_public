package it.mediterraneanrecords.tarotdraw.synonyms

import java.util.Locale

/**
 * Dizionario locale di sinonimi / varianti → lemmi base.
 *
 * Viene usato da MoodEngine per:
 *  - canonicalizzare il testo (trasformare "arrabbiato" in "rabbia", ecc.)
 *  - verificare se esiste almeno UNA parola riconoscibile
 *
 * Restituisce sempre lemmi che il lessico interno di MoodEngine conosce:
 *   "felice","gioia","amore","sereno","calmo","speranza","gratitudine",
 *   "triste","ansia","paura","rabbia","gelosia","solitudine",
 *   "determinato","confuso","stanco",
 *   "lavoro","soldi","stabilita",
 *   "cambiamento","trasformazione","destino"
 */
object LocalSynonymProvider {

    /**
     * Restituisce una lista di possibili lemmi base per il token dato,
     * in base alla lingua (es. "it", "en").
     *
     * Di solito la lista conterrà 0 o 1 elementi.
     */
    fun lookup(token: String, langTag: String): List<String> {
        if (token.isBlank()) return emptyList()

        val norm = token.lowercase(Locale.ROOT).trim()
        val isIt = langTag.startsWith("it", ignoreCase = true)

        val base = if (isIt) {
            IT_NORMALIZE[norm]
        } else {
            EN_NORMALIZE[norm]
            // fallback: se non troviamo nulla in EN, prova comunque la tabella IT
            // (es. usare "rabbia" anche in UI inglese va benissimo, il lessico è italiano)
                ?: IT_NORMALIZE[norm]
        }

        return if (base != null) listOf(base) else emptyList()
    }

    // ==========================
    // ITALIANO: SINONIMI & VARIANTI
    // ==========================

    /**
     * Mappa locale: varianti / sinonimi italiani → lemma base italiano
     * (il lemma base è sempre una chiave del LEXICON_INTERNAL di MoodEngine).
     */
    private val IT_NORMALIZE: Map<String, String> = mapOf(
        // --- emozioni positive → felice / gioia / amore / sereno / calmo / speranza / gratitudine ---

        // felice
        "gioioso" to "felice",
        "gioiosa" to "felice",
        "gioiosi" to "felice",
        "gioiose" to "felice",
        "raggianto" to "felice",
        "raggiante" to "felice",
        "raggianti" to "felice",
        "sollevato" to "felice",
        "sollevata" to "felice",
        "sollevati" to "felice",
        "sollevate" to "felice",
        "contento" to "felice",
        "contenta" to "felice",
        "contenti" to "felice",
        "contente" to "felice",
        "lieto" to "felice",
        "lieta" to "felice",
        "lieti" to "felice",
        "liete" to "felice",

        // gioia
        "entusiasta" to "gioia",
        "entusiaste" to "gioia",
        "entusiasti" to "gioia",
        "entusiasmo" to "gioia",
        "euforico" to "gioia",
        "euforica" to "gioia",
        "euforici" to "gioia",
        "euforiche" to "gioia",
        "ottimista" to "gioia",
        "ottimiste" to "gioia",
        "ottimisti" to "gioia",
        "ottimismo" to "gioia",

        // amore (qui mappiamo alcune sfumature affettive)
        "innamorato" to "amore",
        "innamorata" to "amore",
        "innamorati" to "amore",
        "innamorate" to "amore",
        "affetto" to "amore",
        "affettuoso" to "amore",
        "affettuosa" to "amore",

        // sereno
        "fiducioso" to "sereno",
        "fiduciosa" to "sereno",
        "fiduciosi" to "sereno",
        "fiduciose" to "sereno",
        "soddisfatto" to "sereno",
        "soddisfatta" to "sereno",
        "soddisfatti" to "sereno",
        "soddisfatte" to "sereno",

        // calmo
        "tranquillo" to "calmo",
        "tranquilla" to "calmo",
        "tranquilli" to "calmo",
        "tranquille" to "calmo",
        "rilassato" to "calmo",
        "rilassata" to "calmo",
        "rilassati" to "calmo",
        "rilassate" to "calmo",

        // speranza
        "speranzoso" to "speranza",
        "speranzosa" to "speranza",
        "speranzosi" to "speranza",
        "speranzose" to "speranza",

        // gratitudine
        "grato" to "gratitudine",
        "grata" to "gratitudine",
        "grati" to "gratitudine",
        "grate" to "gratitudine",
        "riconoscente" to "gratitudine",
        "riconoscenti" to "gratitudine",

        // --- emozioni negative → triste / ansia / paura / rabbia / gelosia / solitudine ---

        // contrasto / irritazione → rabbia
        "contrariato" to "rabbia",
        "contrariata" to "rabbia",
        "contrariati" to "rabbia",
        "contrariate" to "rabbia",
        "seccato" to "rabbia",
        "seccata" to "rabbia",
        "seccati" to "rabbia",
        "seccate" to "rabbia",
        "infastidito" to "rabbia",
        "infastidita" to "rabbia",
        "infastiditi" to "rabbia",
        "infastidite" to "rabbia",
        "arrabbiato" to "rabbia",
        "arrabbiata" to "rabbia",
        "arrabbiati" to "rabbia",
        "arrabbiate" to "rabbia",
        "furioso" to "rabbia",
        "furiosa" to "rabbia",
        "furiosi" to "rabbia",
        "furiose" to "rabbia",

        // tristezza / malinconia → triste
        "rattristato" to "triste",
        "rattristata" to "triste",
        "rattristati" to "triste",
        "rattristate" to "triste",
        "malinconico" to "triste",
        "malinconica" to "triste",
        "malinconici" to "triste",
        "malinconiche" to "triste",
        "giù" to "triste",
        "giu" to "triste",
        "abbattuto" to "triste",
        "abbattuta" to "triste",
        "abbattuti" to "triste",
        "abbattute" to "triste",
        "depressa" to "triste",
        "depresso" to "triste",

        // ansia / agitazione → ansia
        "ansioso" to "ansia",
        "ansiosa" to "ansia",
        "ansiosi" to "ansia",
        "ansiose" to "ansia",
        "agitato" to "ansia",
        "agitata" to "ansia",
        "agitati" to "ansia",
        "agitate" to "ansia",
        "preoccupato" to "ansia",
        "preoccupata" to "ansia",
        "preoccupati" to "ansia",
        "preoccupate" to "ansia",

        // paura → paura
        "impaurito" to "paura",
        "impaurita" to "paura",
        "spaventato" to "paura",
        "spaventata" to "paura",
        "terrorizzato" to "paura",
        "terrorizzata" to "paura",

        // gelosia → gelosia
        "geloso" to "gelosia",
        "gelosa" to "gelosia",
        "gelosi" to "gelosia",
        "gelose" to "gelosia",

        // solitudine → solitudine
        "solo" to "solitudine",
        "sola" to "solitudine",
        "isolato" to "solitudine",
        "isolata" to "solitudine",

        // --- energia / azione / lavoro ---

        "determinata" to "determinato",
        "determinati" to "determinato",
        "determinazione" to "determinato",
        "confusa" to "confuso",
        "confusi" to "confuso",
        "confuse" to "confuso",

        "stanchissimo" to "stanco",
        "stanchissima" to "stanco",
        "stanca" to "stanco",

        "occupazione" to "lavoro",
        "impiego" to "lavoro",
        "professionale" to "lavoro",

        "denaro" to "soldi",
        "finanze" to "soldi",
        "economia" to "soldi",

        "stabile" to "stabilita",
        "stabilità" to "stabilita",
        "sicurezza" to "stabilita",

        // --- trasformazione / destino ---

        "cambio" to "cambiamento",
        "cambiare" to "cambiamento",
        "trasformarsi" to "trasformazione",
        "trasformato" to "trasformazione",
        "kismet" to "destino",
        "fato" to "destino",
        "sorte" to "destino"
    )

    // ==========================
    // INGLESE: SINONIMI & VARIANTI
    // ==========================

    /**
     * Mappa minima per l'inglese.
     * Anche qui i lemmi finali sono SEMPRE quelli italiani noti al lessico.
     * (es. "angry" → "rabbia")
     */
    private val EN_NORMALIZE: Map<String, String> = mapOf(
        // positive
        "happy" to "felice",
        "happiness" to "felice",
        "joy" to "gioia",
        "joyful" to "gioia",
        "in love" to "amore",
        "loving" to "amore",
        "peaceful" to "sereno",
        "calm" to "calmo",
        "hope" to "speranza",
        "hopeful" to "speranza",
        "grateful" to "gratitudine",
        "thankful" to "gratitudine",

        // negative
        "sad" to "triste",
        "sadness" to "triste",
        "depressed" to "triste",
        "blue" to "triste",

        "anxious" to "ansia",
        "anxiety" to "ansia",
        "worried" to "ansia",
        "worry" to "ansia",

        "afraid" to "paura",
        "scared" to "paura",
        "fear" to "paura",
        "terrified" to "paura",

        "angry" to "rabbia",
        "anger" to "rabbia",
        "furious" to "rabbia",
        "annoyed" to "rabbia",
        "upset" to "rabbia",

        "jealous" to "gelosia",
        "envy" to "gelosia",

        "lonely" to "solitudine",
        "isolated" to "solitudine",

        "determined" to "determinato",
        "confused" to "confuso",
        "tired" to "stanco",
        "exhausted" to "stanco",

        "work" to "lavoro",
        "job" to "lavoro",
        "money" to "soldi",
        "wealth" to "soldi",
        "stability" to "stabilita",
        "secure" to "stabilita",

        "change" to "cambiamento",
        "changing" to "cambiamento",
        "transformation" to "trasformazione",
        "transforming" to "trasformazione",
        "fate" to "destino",
        "destiny" to "destino"
    )
}