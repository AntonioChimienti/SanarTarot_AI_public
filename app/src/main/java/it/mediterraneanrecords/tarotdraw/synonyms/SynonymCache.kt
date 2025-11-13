package it.mediterraneanrecords.tarotdraw.synonyms

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "syn_cache")
private val KEY_MAP = stringPreferencesKey("map_json")

object SynonymCache {

    /** word -> base */
    fun get(ctx: Context, word: String): String? = runBlocking {
        val prefs = ctx.dataStore.data.first()
        val json = prefs[KEY_MAP] ?: return@runBlocking null
        val obj = JSONObject(json)
        obj.optString(word.lowercase()).takeIf { it.isNotBlank() }
    }

    fun put(ctx: Context, word: String, base: String) = runBlocking {
        ctx.dataStore.edit { prefs ->
            val current = prefs[KEY_MAP]?.let { JSONObject(it) } ?: JSONObject()
            current.put(word.lowercase(), base)
            prefs[KEY_MAP] = current.toString()
        }
    }
}