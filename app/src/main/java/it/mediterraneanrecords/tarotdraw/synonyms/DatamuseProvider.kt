// DatamuseProvider.kt
package it.mediterraneanrecords.tarotdraw.synonyms

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

object DatamuseProvider : SynonymProvider {
    private val client = OkHttpClient()

    override suspend fun lookup(word: String, langTag: String): List<String> =
        withContext(Dispatchers.IO) {
            try {
                // ✅ URL corretto (datamuse, non datamIse)
                val url = "https://api.datamuse.com/words?ml=${word.trim()}"

                val req = Request.Builder().url(url).get().build()
                client.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) return@withContext emptyList<String>()
                    val body = resp.body?.string() ?: return@withContext emptyList<String>()
                    val arr = JSONArray(body)
                    val out = mutableListOf<String>()
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        val w = o.optString("word").lowercase()
                        if (w.isNotBlank()) out += w
                    }
                    out.distinct()
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
}