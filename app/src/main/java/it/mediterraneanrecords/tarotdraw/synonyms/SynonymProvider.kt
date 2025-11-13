package it.mediterraneanrecords.tarotdraw.synonyms

interface SynonymProvider {
    /** Ritorna possibili lemmi/sinonimi per la singola parola (NO frasi). */
    suspend fun lookup(word: String, langTag: String): List<String>
}