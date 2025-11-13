# TarotDraw – Full Edition (Visconti–Sforza)

Contiene progetto Android completo + 78 **segnaposto** con i nomi esatti.
Sostituiscili con le immagini pubbliche del mazzo Visconti–Sforza.

## Import rapido delle immagini
Metti le 78 immagini reali in una cartella, poi in PowerShell/cmd:
```
python tools/import_cards.py "C:\percorso\cartella\immagini"
```
Importerà e rinominerà i file in `app/src/main/res/drawable`.

## Build APK
Android Studio → File > Open… → cartella progetto → Build > Build APK(s)
APK: `app/build/outputs/apk/debug/app-debug.apk`