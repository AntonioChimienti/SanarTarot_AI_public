import sys, re, shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DRAWABLE = ROOT / "app" / "src" / "main" / "res" / "drawable"

EXPECTED = [
    "0 - Il Matto","I - Il Bagatto","II - La Papessa","III - L'Imperatrice","IV - L'Imperatore",
    "V - Il Papa","VI - Gli Amanti","VII - Il Carro","VIII - La Giustizia","IX - L'Eremita",
    "X - La Ruota della Fortuna","XI - La Forza","XII - L'Appeso","XIII - La Morte","XIV - La Temperanza",
    "XV - Il Diavolo","XVI - La Torre","XVII - Le Stelle","XVIII - La Luna","XIX - Il Sole","XX - Il Giudizio","XXI - Il Mondo"
]
def suit(name):
    return [f"Asso di {name}"] + [f"{i} di {name}" for i in range(2,11)] + [f"Fante di {name}", f"Cavaliere di {name}", f"Regina di {name}", f"Re di {name}"]
EXPECTED += suit("Bastoni")+suit("Coppe")+suit("Spade")+suit("Denari")

def res_name(n, i):
    s = n.lower()
    for a,b in [("à","a"),("è","e"),("é","e"),("ì","i"),("ò","o"),("ù","u")]:
        s = s.replace(a,b)
    s = s.replace("'","" ).replace("(","").replace(")","" )
    s = re.sub(r"[^a-z0-9]+","_", s).strip("_")
    return f"card_{i:02d}_{s}"

def main():
    if len(sys.argv) < 2:
        print("Usage: python tools/import_cards.py C:/path/folder")
        sys.exit(1)
    src = Path(sys.argv[1])
    if not src.exists():
        print("Source not found:", src)
        sys.exit(1)
    DRAWABLE.mkdir(parents=True, exist_ok=True)
    files = sorted([p for p in src.iterdir() if p.is_file()])
    if not files:
        print("No files in", src)
        sys.exit(1)
    count = 0
    for i, name in enumerate(EXPECTED):
        if i < len(files):
            target = DRAWABLE / f"{res_name(name,i)}.png"
            shutil.copy(files[i], target)
            count += 1
    print(f"Imported {count} images into {DRAWABLE}")

if __name__ == "__main__":
    main()