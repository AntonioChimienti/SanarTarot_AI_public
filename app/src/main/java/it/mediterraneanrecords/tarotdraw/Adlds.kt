package it.mediterraneanrecords.tarotdraw

object AdIds {

    // Banner
    const val BANNER_TEST = "ca-app-pub-3940256099942544/6300978111"
    const val BANNER_REAL = "ca-app-pub-8159860715703754/8370661219"

    // Interstitial
    const val INTERSTITIAL_TEST = "ca-app-pub-3940256099942544/1033173712"
    const val INTERSTITIAL_REAL = "ca-app-pub-8159860715703754/3931070369"

    // 🔁 Interruttore manuale: TRUE = usa ID di test, FALSE = usa quelli reali
    //private const val USE_TEST_ADS = true
    private const val USE_TEST_ADS = false

    fun bannerId(): String =
        if (USE_TEST_ADS) BANNER_TEST else BANNER_REAL

    fun interstitialId(): String =
        if (USE_TEST_ADS) INTERSTITIAL_TEST else INTERSTITIAL_REAL
}