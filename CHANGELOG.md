## v1.5.1 – ARC deck & audio polish (2025-11-13)

### Fixes
- Corretto caricamento delle immagini del mazzo ARC (sanato il mismatch tra id mazzo e prefisso delle risorse).
- Le risorse ARC con prefisso `arc_` ora vengono risolte correttamente in `cardDrawableResForDeck`.
- Centralizzata la gestione dell’audio ambientale tramite `AmbientAudio` con flag globale `isEnabled`.
- Rimossa la vecchia `AmbientSoundController` per evitare doppio `MediaPlayer` e comportamenti incoerenti.
- Evitato che Estrai / Reset riattivino l’audio quando l’utente l’ha esplicitamente disattivato.

### UX / i18n
- Normalizzato l’id mazzo per **Arcana Nova** a `"arc"` nel selettore mazzi.
- Aggiunta traduzione inglese per la voce di menu “Choose deck”.

### Note
- Questa versione è la candidata per la pubblicazione su Google Play di *Sanar Tarot Draw*.