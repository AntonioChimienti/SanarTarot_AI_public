package it.mediterraneanrecords.tarotdraw

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// DataStore: definito UNA SOLA volta a livello di file
private val Context.dataStore by preferencesDataStore(name = "settings")

object ApiConfig {

    // === Chiavi DataStore ===
    private val KEY_PROVIDER = stringPreferencesKey("ai_provider")

    private val KEY_OPENAI = stringPreferencesKey("openai_key")
    private val KEY_GEMINI = stringPreferencesKey("gemini_key")
    private val KEY_LANG = stringPreferencesKey("lang")     // "it" | "en"
    private val KEY_DECK = stringPreferencesKey("deck")     // "base" | "lux" | "cel" | "nova"
    private val KEY_BG_DIM = floatPreferencesKey("bg_dim") // 0.0..1.0

    // === Flussi lettura preferenze ===
    fun providerFlow(ctx: Context): Flow<AiProvider> =
        ctx.dataStore.data.map { pref ->
            when (pref[KEY_PROVIDER]) {
                "OPENAI" -> AiProvider.OPENAI
                "GEMINI" -> AiProvider.GEMINI
                else -> AiProvider.NONE
            }
        }

    fun openAiKeyFlow(ctx: Context): Flow<String> =
        ctx.dataStore.data.map { it[KEY_OPENAI] ?: "" }

    fun geminiKeyFlow(ctx: Context): Flow<String> =
        ctx.dataStore.data.map { it[KEY_GEMINI] ?: "" }

    fun langFlow(ctx: Context): Flow<String> =
        ctx.dataStore.data.map { it[KEY_LANG] ?: "it" }

    fun deckFlow(ctx: Context): Flow<String> =
        ctx.dataStore.data.map { it[KEY_DECK] ?: "base" }

    // === Flusso luminosità sfondo ===
    fun bgDimFlow(ctx: Context): Flow<Float> =
        ctx.dataStore.data.map { pref ->
            pref[KEY_BG_DIM] ?: 0.65f    // default 65%
        }

    suspend fun setBgDim(ctx: Context, value: Float) {
        val v = value.coerceIn(0f, 0.85f)  // limiti di sicurezza
        ctx.dataStore.edit { it[KEY_BG_DIM] = v }
    }

    // === Scrittura preferenze ===
    suspend fun setProvider(ctx: Context, p: AiProvider) {
        ctx.dataStore.edit { it[KEY_PROVIDER] = p.name }
    }

    suspend fun setOpenAiKey(ctx: Context, v: String) {
        ctx.dataStore.edit { it[KEY_OPENAI] = v.trim() }
    }

    suspend fun setGeminiKey(ctx: Context, v: String) {
        ctx.dataStore.edit { it[KEY_GEMINI] = v.trim() }
    }

    suspend fun setLang(ctx: Context, v: String) {
        ctx.dataStore.edit { it[KEY_LANG] = v }
    }

    suspend fun setDeck(ctx: Context, value: String) {
        ctx.dataStore.edit { it[KEY_DECK] = value }
    }
}