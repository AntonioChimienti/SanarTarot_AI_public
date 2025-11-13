package it.mediterraneanrecords.tarotdraw

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.util.concurrent.TimeUnit


/**
 * Client “robusto ma semplice” per chiamate AI.
 * - Se la chiave è vuota -> non chiama nulla e restituisce default
 * - Parsing permissivo: tenta di leggere JSON o testo, altrimenti default
 *
 * Richiede in build.gradle:
 *   implementation("com.squareup.okhttp3:okhttp:4.12.0")
 *   implementation("com.google.code.gson:gson:2.10.1")
 */
object LlmClient {

    private const val TAG = "LlmClient"
    private val gson = Gson()
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .callTimeout(40, TimeUnit.SECONDS)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(40, TimeUnit.SECONDS)
            .build()
    }
    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    /* -----------------------------------------------------------
     * OPENAI
     * ----------------------------------------------------------- */

    /**
     * Chiede a OpenAI di trasformare un prompt “mood” in boost numerici.
     * Ritorna null se key assente/errore (l’app poi userà solo la logica locale).
     */
    suspend fun fetchBoostsFromOpenAI(
        apiKey: String,
        prompt: String,
        model: String = "gpt-4o-mini",
    ): LlmBoosts? = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext null
        try {
            val url = "https://api.openai.com/v1/chat/completions"
            // Prompt di sistema: chiediamo un JSON chiaro
            val sys = """
                Sei un assistente tarologico. 
                Riceverai un testo di "stato d'animo".
                Rispondi SOLO in JSON con i seguenti campi:
                {
                  "majors": number (1.0 = neutro, 0.0..2.0),
                  "wands": number,
                  "cups": number,
                  "swords": number,
                  "pentacles": number,
                  "specificMajors": [interi tra 0 e 21],
                  "specificMajorsBoost": number (0..2)
                }
            """.trimIndent()

            val bodyObj = mapOf(
                "model" to model,
                "temperature" to 0.2,
                "response_format" to mapOf("type" to "json_object"),
                "messages" to listOf(
                    mapOf("role" to "system", "content" to sys),
                    mapOf("role" to "user", "content" to prompt)
                )
            )
            val body = RequestBody.create(jsonMedia, gson.toJson(bodyObj))
            val req = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build()

            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    Log.w(TAG, "OpenAI HTTP ${resp.code}")
                    return@withContext null
                }
                val txt = resp.body?.string().orEmpty()
                // Struttura tipica: choices[0].message.content (JSON string)
                val root = gson.fromJson(txt, JsonObject::class.java)
                val choices = root["choices"]?.asJsonArray
                val content = choices?.firstOrNull()
                    ?.asJsonObject?.get("message")?.asJsonObject
                    ?.get("content")?.asString.orEmpty()

                // Prova a interpretare direttamente il content come JSON
                val json = runCatching {
                    gson.fromJson(content, JsonObject::class.java)
                }.getOrNull()

                return@withContext json?.toBoostsOrNull()
            }
        } catch (t: Throwable) {
            Log.e(TAG, "fetchBoostsFromOpenAI error", t)
            null
        }
    }

    /**
     * Interpreta un testo “Visione” in 0..N Arcani Maggiori suggeriti.
     * Se parsing fallisce -> lista vuota.
     */
    suspend fun visionToMajorsOpenAI(
        apiKey: String,
        text: String,
        model: String = "gpt-4o-mini",
    ): LlmResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext LlmResult()
        try {
            val url = "https://api.openai.com/v1/chat/completions"
            val sys = """
                Riceverai un breve testo chiamato "Visione".
                Rispondi SOLO in JSON, con:
                { "suggestedMajors": [interi tra 0 e 21] }
                Non aggiungere spiegazioni.
            """.trimIndent()
            val bodyObj = mapOf(
                "model" to model,
                "temperature" to 0.2,
                "response_format" to mapOf("type" to "json_object"),
                "messages" to listOf(
                    mapOf("role" to "system", "content" to sys),
                    mapOf("role" to "user", "content" to text)
                )
            )
            val body = RequestBody.create(jsonMedia, gson.toJson(bodyObj))
            val req = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build()

            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext LlmResult()
                val txt = resp.body?.string().orEmpty()
                val root = gson.fromJson(txt, JsonObject::class.java)
                val content = root["choices"]?.asJsonArray?.firstOrNull()
                    ?.asJsonObject?.get("message")?.asJsonObject
                    ?.get("content")?.asString.orEmpty()
                val json =
                    runCatching { gson.fromJson(content, JsonObject::class.java) }.getOrNull()
                val arr = json?.getAsJsonArray("suggestedMajors")
                val list = arr?.mapNotNull { it.asIntOrNullInRange(0, 21) } ?: emptyList()
                return@withContext LlmResult(boosts = LlmBoosts(specificMajors = list.toSet()))
            }
        } catch (t: Throwable) {
            Log.e(TAG, "visionToMajorsOpenAI error", t)
            LlmResult()
        }
    }

    /* -----------------------------------------------------------
     * GEMINI
     * ----------------------------------------------------------- */

    suspend fun visionToMajorsGemini(
        apiKey: String,
        text: String,
        model: String = "gemini-1.5-flash",
    ): LlmResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext LlmResult()
        try {
            // Endpoint REST pubblico di Gemini (text-only)
            val url =
                "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

            val sys =
                "Rispondi SOLO in JSON: { \"suggestedMajors\": [interi tra 0 e 21] }"
            val bodyObj = mapOf(
                "contents" to listOf(
                    mapOf(
                        "parts" to listOf(
                            mapOf("text" to sys),
                            mapOf("text" to text)
                        )
                    )
                )
            )
            val body = RequestBody.create(jsonMedia, gson.toJson(bodyObj))
            val req = Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build()

            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext LlmResult()
                val txt = resp.body?.string().orEmpty()
                val root = gson.fromJson(txt, JsonObject::class.java)
                // Percorso tipico: candidates[0].content.parts[0].text
                val textOut = root["candidates"]?.asJsonArray?.firstOrNull()
                    ?.asJsonObject?.get("content")?.asJsonObject
                    ?.get("parts")?.asJsonArray?.firstOrNull()
                    ?.asJsonObject?.get("text")?.asString.orEmpty()

                val json =
                    runCatching { gson.fromJson(textOut, JsonObject::class.java) }.getOrNull()
                val arr = json?.getAsJsonArray("suggestedMajors")
                val list = arr?.mapNotNull { it.asIntOrNullInRange(0, 21) } ?: emptyList()
                return@withContext LlmResult(boosts = LlmBoosts(specificMajors = list.toSet()))
            }
        } catch (t: Throwable) {
            Log.e(TAG, "visionToMajorsGemini error", t)
            LlmResult()
        }
    }

    /* -----------------------------------------------------------
     * Helpers
     * ----------------------------------------------------------- */

    private fun JsonObject.toBoostsOrNull(): LlmBoosts? = try {
        LlmBoosts(
            majors = this["majors"]?.asFloatOrDefault(1f) ?: 1f,
            wands = this["wands"]?.asFloatOrDefault(1f) ?: 1f,
            cups = this["cups"]?.asFloatOrDefault(1f) ?: 1f,
            swords = this["swords"]?.asFloatOrDefault(1f) ?: 1f,
            pentacles = this["pentacles"]?.asFloatOrDefault(1f) ?: 1f,
            specificMajors = this["specificMajors"]?.asJsonArray
                ?.mapNotNull { it.asIntOrNullInRange(0, 21) }?.toSet() ?: emptySet(),
            specificMajorsBoost = this["specificMajorsBoost"]?.asFloatOrDefault(0f) ?: 0f
        ).clamped()
    } catch (_: Throwable) {
        null
    }

    private fun LlmBoosts.clamped(): LlmBoosts =
        copy(
            majors = majors.coerceIn(0f, 2f),
            wands = wands.coerceIn(0f, 2f),
            cups = cups.coerceIn(0f, 2f),
            swords = swords.coerceIn(0f, 2f),
            pentacles = pentacles.coerceIn(0f, 2f),
            specificMajors = specificMajors.filter { it in 0..21 }.toSet(),
            specificMajorsBoost = specificMajorsBoost.coerceIn(0f, 2f)
        )

    private fun JsonElement.asFloatOrDefault(def: Float): Float = runCatching {
        when {
            isJsonPrimitive && asJsonPrimitive.isNumber -> asFloat
            isJsonPrimitive && asJsonPrimitive.isString -> asString.toFloat()
            else -> def
        }
    }.getOrElse { def }

    private fun JsonElement.asIntOrNullInRange(min: Int, max: Int): Int? = runCatching {
        when {
            isJsonPrimitive && asJsonPrimitive.isNumber -> asInt
            isJsonPrimitive && asJsonPrimitive.isString -> asString.toInt()
            else -> null
        }?.takeIf { it in min..max }
    }.getOrNull()
}