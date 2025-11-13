package it.mediterraneanrecords.tarotdraw.synonyms

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Minimal classifier: dato un termine, chiede il lemma emozionale tra un set chiuso.
 * Usa l'API key passata (NON quella di BuildConfig).
 */
object OpenAIProvider : SynonymProvider {
    private val client = OkHttpClient()
    private val json = "application/json".toMediaType()

    private val LABELS = listOf(
        "felice", "gioia", "amore", "sereno", "calmo", "speranza", "gratitudine",
        "triste", "ansia", "paura", "rabbia", "gelosia", "solitudine",
        "determinato", "confuso", "stanco",
        "lavoro", "soldi", "stabilita",
        "cambiamento", "trasformazione", "destino"
    )

    override suspend fun lookup(word: String, langTag: String): List<String> {
        return emptyList() // usiamo la funzione dedicata sotto
    }

    suspend fun classifyToBase(word: String, langTag: String, apiKey: String): String? {
        if (apiKey.isBlank()) return null
        val prompt = """
            Restituisci SOLO una delle seguenti etichette che meglio rappresenta la parola "$word":
            ${LABELS.joinToString(", ")}.
            Se nessuna è adatta, rispondi "none".
        """.trimIndent()

        val msg = JsonArray().apply {
            add(JsonObject().apply {
                addProperty("role", "user")
                addProperty("content", prompt)
            })
        }
        val body = JsonObject().apply {
            addProperty("model", "gpt-4o-mini")
            add("messages", msg)
            addProperty("temperature", 0.0)
        }.toString().toRequestBody(json)

        val req = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body).build()

        val resp = client.newCall(req).execute()
        if (!resp.isSuccessful) return null
        val txt = resp.body?.string() ?: return null
        // parsing minimo
        val m = Regex("\"content\"\\s*:\\s*\"([^\"]+)\"").find(txt) ?: return null
        val label = m.groupValues[1].trim().lowercase()
        return label.takeIf { it in LABELS }
    }
}