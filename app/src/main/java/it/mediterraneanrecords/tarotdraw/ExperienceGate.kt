package it.mediterraneanrecords.tarotdraw

import android.content.Context
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import it.mediterraneanrecords.tarotdraw.BuildFlavor
object ExperienceGate {

    // -----------------------------------------------------
    //  CONFIGURAZIONE VERSIONE GRATUITA
    // -----------------------------------------------------
    private const val FREE_MAX_DRAWS = 30          // 30 stese
    private const val FREE_TRIAL_DAYS = 10         // 10 giorni di utilizzo
    private const val PREF_NAME = "experience_gate"

    // Flag letto da BuildFlavor.kt (FREE / PRO)
    val isPro: Boolean
        get() = BuildFlavor.IS_PRO

    // -----------------------------------------------------
    //  INCREMENTA I CONTATORI (FREE MODE)
    // -----------------------------------------------------
    fun registerDraw(ctx: Context) {
        if (isPro) return  // nessun limite

        val prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val count = prefs.getInt("draw_count", 0) + 1
        prefs.edit().putInt("draw_count", count).apply()
    }

    // -----------------------------------------------------
    //  VERIFICA SE L’UTENTE HA SUPERATO I LIMITI
    // -----------------------------------------------------
    fun checkLimits(ctx: Context): Boolean {
        if (isPro) return false

        val prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        // Primo avvio → salva timestamp
        if (!prefs.contains("first_launch")) {
            prefs.edit()
                .putLong("first_launch", System.currentTimeMillis())
                .apply()
        }

        val start = prefs.getLong("first_launch", System.currentTimeMillis())
        val days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - start)

        val draws = prefs.getInt("draw_count", 0)

        return (days >= FREE_TRIAL_DAYS || draws >= FREE_MAX_DRAWS)
    }

    // -----------------------------------------------------
    //  CHIAMATA UNICA DAL MAINACTIVITY
    // -----------------------------------------------------
    fun checkAndPrompt(
        context: Context,
        onLimitReached: () -> Unit
    ) {
        if (!isPro && checkLimits(context)) {
            onLimitReached()
        }
    }
}