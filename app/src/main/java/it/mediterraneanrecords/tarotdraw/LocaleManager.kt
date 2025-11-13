package it.mediterraneanrecords.tarotdraw

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LocaleManager {
    private const val PREFS = "locale_prefs"
    private const val KEY = "app_lang"

    fun getSavedLang(ctx: Context): String {
        val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return p.getString(KEY, "it") ?: "it"
    }

    fun persist(ctx: Context, lang: String) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, lang)
            .apply()
    }

    fun wrapContext(ctx: Context): Context {
        val lang = getSavedLang(ctx)
        return wrap(ctx, lang)
    }

    fun wrap(ctx: Context, lang: String): Context {
        val locale = Locale.forLanguageTag(lang)
        Locale.setDefault(locale)

        val config = Configuration(ctx.resources.configuration)
        if (Build.VERSION.SDK_INT >= 33) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.setLocale(locale)
        }
        return ctx.createConfigurationContext(config)
    }
}
