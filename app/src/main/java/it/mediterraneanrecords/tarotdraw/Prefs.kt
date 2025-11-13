package it.mediterraneanrecords.tarotdraw

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Extension UNICA del progetto
val Context.tarotDataStore by preferencesDataStore("tarot_prefs")

object Prefs {
    private val LAST_DRAW = stringSetPreferencesKey("last_draw") // "index|rev"
    private val LAST_COUNT = intPreferencesKey("last_count")

    suspend fun save(ctx: Context, drawn: List<DrawnCard>, count: Int) {
        ctx.tarotDataStore.edit { p ->
            p[LAST_COUNT] = count
            p[LAST_DRAW] = drawn.map { "${it.card.index}|${if (it.reversed) 1 else 0}" }.toSet()

        }
    }

    suspend fun load(ctx: Context): Pair<Int, List<DrawnCard>> {
        val flow = ctx.tarotDataStore.data.map { p ->
            val count = p[LAST_COUNT] ?: 5
            val set = p[LAST_DRAW].orEmpty()
            val list = set.mapNotNull { line ->
                val parts = line.split("|")
                val idx = parts.getOrNull(0)?.toIntOrNull() ?: return@mapNotNull null
                val rev = parts.getOrNull(1) == "1"
                val card = TarotDeck.cards.getOrNull(idx) ?: return@mapNotNull null
                DrawnCard(card, rev)
            }
            count to list
        }
        return flow.first()
    }

    suspend fun clear(ctx: Context) {
        ctx.tarotDataStore.edit { it.clear() }
    }
}