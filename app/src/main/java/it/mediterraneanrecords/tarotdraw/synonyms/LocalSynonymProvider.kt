package it.mediterraneanrecords.tarotdraw.synonyms

/** Mappa locale leggera: sinonimi/varianti -> lemma base (IT). Espandibile. */
object LocalSynonymProvider : SynonymProvider {

    // Lemmi effettivamente presenti nel lessico interno / assets
    private val KNOWN_BASES = setOf(
        "felice", "gioia", "amore", "sereno", "calmo", "speranza", "gratitudine",
        "triste", "ansia", "paura", "rabbia", "gelosia", "solitudine",
        "determinato", "confuso", "stanco",
        "lavoro", "soldi", "stabilita",
        "cambiamento", "trasformazione", "destino"
    )

    private val SYNONYM_TO_BASE = mapOf(
        // allegria / contentezza -> felice
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

        // entusiasmo / ottimismo -> gioia
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

        // fiducia / speranza
        "fiducioso" to "sereno",
        "fiduciosa" to "sereno",
        "fiduciosi" to "sereno",
        "fiduciose" to "sereno",
        "speranzoso" to "speranza",
        "speranzosa" to "speranza",
        "speranzosi" to "speranza",

        // gratitudine / soddisfazione
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

        // calma / tranquillità
        "tranquillo" to "calmo",
        "tranquilla" to "calmo",
        "tranquilli" to "calmo",
        "tranquille" to "calmo",
        "rilassato" to "calmo",
        "rilassata" to "calmo",
        "rilassati" to "calmo",

        // down / giù -> triste
        "rattristato" to "triste",
        "rattristata" to "triste",
        "rattristati" to "triste",
        "rattristate" to "triste",
        "malinconico" to "triste",
        "malinconica" to "triste",
        "melanconico" to "triste",
        "melanconica" to "triste",
        "avvilito" to "triste",
        "avvilita" to "triste",

        // fastidio / irritazione -> rabbia
        "contrariato" to "rabbia",
        "contrariata" to "rabbia",
        "seccato" to "rabbia",
        "seccata" to "rabbia",
        "infastidito" to "rabbia",
        "infastidita" to "rabbia",
        "irritato" to "rabbia",
        "irritata" to "rabbia",

        // ansia / inquietudine / paura
        "inansia" to "ansia",
        "ansioso" to "ansia",
        "ansiosa" to "ansia",
        "angosciato" to "ansia",
        "angosciata" to "ansia",
        "inquieto" to "ansia",
        "inquieta" to "ansia",
        "preoccupato" to "ansia",
        "preoccupata" to "ansia",
        "impaurito" to "paura",
        "impaurita" to "paura",
        "spaventato" to "paura",
        "spaventata" to "paura"
    )

    /** Prefissi robusti -> base */
    private val ROOT_TO_BASE = listOf(
        "rattristat" to "triste",
        "malincon" to "triste",
        "melancon" to "triste",
        "avvil" to "triste",
        "contrariat" to "rabbia",
        "seccat" to "rabbia",
        "infastid" to "rabbia",
        "irritat" to "rabbia",
        "ansios" to "ansia",
        "angosci" to "ansia",
        "inquiet" to "ansia",
        "preoccupat" to "ansia",
        "impaurit" to "paura",
        "spavent" to "paura",
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

    override suspend fun lookup(word: String, langTag: String): List<String> {
        val w = word.lowercase()
        SYNONYM_TO_BASE[w]?.let { return listOf(it) }
        ROOT_TO_BASE.firstOrNull { w.startsWith(it.first) }?.let { return listOf(it.second) }
        return if (w in KNOWN_BASES) listOf(w) else emptyList()
    }
}