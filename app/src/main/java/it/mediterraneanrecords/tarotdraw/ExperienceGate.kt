package it.mediterraneanrecords.tarotdraw

import android.content.Context
import java.util.concurrent.TimeUnit

data class ExperienceState(
    val daysSinceInstall: Int,
    val drawsCount: Int,
    val honeymoon: Boolean      // true = periodo “tutto sbloccato”
)

object ExperienceGate {

    private const val PREFS_NAME = "sanar_experience"
    private const val KEY_INSTALL_TS = "install_ts"
    private const val KEY_DRAWS = "draws_count"

    // parametri “luna di miele”
    private const val HONEYMOON_DAYS = 10
    private const val HONEYMOON_MAX_DRAWS = 30

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadState(ctx: Context): ExperienceState {
        val p = prefs(ctx)
        val now = System.currentTimeMillis()

        // se è la prima volta, salvo ora come “install_ts”
        val installTs = p.getLong(KEY_INSTALL_TS, 0L).let { saved ->
            if (saved == 0L) {
                p.edit().putLong(KEY_INSTALL_TS, now).apply()
                now
            } else saved
        }

        val diffMs = now - installTs
        val days = TimeUnit.MILLISECONDS.toDays(diffMs).toInt().coerceAtLeast(0)

        val draws = p.getInt(KEY_DRAWS, 0).coerceAtLeast(0)

        val honeymoon = (days < HONEYMOON_DAYS) && (draws < HONEYMOON_MAX_DRAWS)

        return ExperienceState(
            daysSinceInstall = days,
            drawsCount = draws,
            honeymoon = honeymoon
        )
    }

    /** Da chiamare ogni volta che l’utente fa una stesa completa */
    fun registerDraw(ctx: Context): ExperienceState {
        val p = prefs(ctx)
        val current = p.getInt(KEY_DRAWS, 0).coerceAtLeast(0) + 1
        p.edit().putInt(KEY_DRAWS, current).apply()
        // ricalcola lo stato completo
        return loadState(ctx)
    }
}