package it.mediterraneanrecords.tarotdraw

import it.mediterraneanrecords.tarotdraw.BuildConfig  // ✅ IMPORT GIUSTO

/**
 * Unico wrapper per sapere se la build è PRO o FREE.
 * Legge il valore dal BuildConfig generato dai productFlavors.
 */
object BuildFlavor {
    val IS_PRO: Boolean
        get() = BuildConfig.IS_PRO
}