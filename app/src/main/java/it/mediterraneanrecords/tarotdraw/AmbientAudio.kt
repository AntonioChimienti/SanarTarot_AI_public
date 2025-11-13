package it.mediterraneanrecords.tarotdraw

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes

object AmbientAudio {
    private var player: MediaPlayer? = null
    private var currentRes: Int? = null

    // Flag globale: se è false, niente suono parte, anche se qualcuno chiama play()
    private var isEnabled: Boolean = true

    /**
     * Abilita o disabilita globalmente l'audio ambientale.
     * Se viene impostato a false interrompe subito l'eventuale musica in corso.
     */
    fun setEnabled(enabled: Boolean, ctx: Context? = null) {
        isEnabled = enabled
        if (!enabled) {
            // Se spengo l'audio, fermo qualsiasi cosa stia suonando
            stop()
        } else {
            // Se vorrai far ripartire l'ultima traccia automaticamente,
            // puoi usare currentRes + ctx qui. Per ora NON facciamo nulla.
        }
    }

    /**
     * Ritorna lo stato corrente del flag audio.
     */
    fun isEnabled(): Boolean = isEnabled

    /**
     * Avvia la riproduzione di una traccia ambientale.
     * Se l'audio è stato disattivato (isEnabled == false) NON parte nulla.
     */
    fun play(ctx: Context, @RawRes resId: Int, loop: Boolean) {
        // Se l'utente ha spento l'audio, esci subito
        if (!isEnabled) return

        // Se è già in riproduzione la stessa risorsa, non fare nulla
        if (currentRes == resId && player?.isPlaying == true) return

        // Chiudi eventuale player precedente
        stop()

        // Crea un nuovo MediaPlayer per la risorsa richiesta
        player = MediaPlayer.create(ctx, resId)?.apply {
            isLooping = loop
            setVolume(0.35f, 0.35f)
            try {
                start()
                currentRes = resId
            } catch (_: Throwable) {
                // Se qualcosa va storto, azzera il player
                stop()
            }
        }
    }

    /**
     * Ferma e rilascia il player corrente, se presente.
     */
    fun stop() {
        player?.let {
            try {
                if (it.isPlaying) {
                    it.stop()
                }
            } catch (_: Throwable) {
            }
            try {
                it.release()
            } catch (_: Throwable) {
            }
        }
        player = null
        currentRes = null
    }
}