"""Íconos 2D (16x16) de las reliquias de movilidad: guantes de seda, guantes de la viuda, breeze en una botella
(capa sobre la botella de vidrio vanilla), botas de magma y cuernos del carnero.

Salida: <id>.png (y breeze_bottle_overlay.png) + hoja movilidad.png para revisar.
Uso: python mobility_icons.py <carpeta_salida> <glass_bottle.png vanilla (solo para la vista previa)>
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mclib import H, T, png  # noqa: E402

GLOVE = [
    "................",
    "....k.k.k.......",
    "...kakakak......",
    "...kakakak......",
    "...kakakak.kk...",
    "...kaaaaaakak...",
    "...kaaaaaaaak...",
    "...kaaaaaaak....",
    "...kaaaaaaak....",
    "....kaaaaak.....",
    "....kbbbbbk.....",
    "....kBBBBBk.....",
    "....kbbbbbk.....",
    ".....kkkkk......",
    "................",
    "................",
]
# Seda: blanca con hebras de telaraña.  Viuda: negra con el reloj de arena rojo en el dorso.
SILK = {'.': T, 'k': H('#3a3a44'), 'a': H('#ecebf0'), 'b': H('#b9b8c4'), 'B': H('#d6d5de'), 'w': H('#9c9ba8')}
WIDOW = {'.': T, 'k': H('#0c0a10'), 'a': H('#2c2433'), 'b': H('#41364b'), 'B': H('#56485f'), 'r': H('#d1242b')}
SILK_MARKS = [(5, 5, 'w'), (6, 6, 'w'), (7, 7, 'w'), (8, 6, 'w'), (9, 5, 'w'), (6, 8, 'w'), (8, 8, 'w')]
WIDOW_MARKS = [(6, 6, 'r'), (7, 6, 'r'), (8, 6, 'r'), (7, 7, 'r'), (6, 8, 'r'), (7, 8, 'r'), (8, 8, 'r')]

# Remolino de viento dentro de la botella de vidrio vanilla
BOTTLE_WIND = [
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
    "........W.......",
    "........vVW.....",
    ".......W...v....",
    "......v..W.v....",
    "......v.Vv.W....",
    ".......W..v.....",
    "........vW......",
    "................",
    "................",
]
WIND = {'.': T, 'W': H('#effdff'), 'V': H('#bdf0f7'), 'v': H('#79cfe0')}

BOOTS = [
    "................",
    "................",
    "..kkkk..........",
    "..kbBk...kkkk...",
    "..kBok...kbBk...",
    "..kbBk...kBok...",
    "..kOBk...kbBk...",
    "..kbBkk..kOBkk..",
    "..kBboBk.kbBboBk",
    "..kbOBbk.kBbOBbk",
    "..ksssskkkssssk.",
    "..kkkkkk.kkkkkk.",
    "................",
    "................",
    "................",
    "................",
]
MAGMA = {'.': T, 'k': H('#120c0c'), 'b': H('#2c2525'), 'B': H('#413737'), 'o': H('#ff7a1a'), 'O': H('#ffc23a'),
         's': H('#6b2a14')}

HORNS = [
    "................",
    "................",
    "................",
    "..kkkk....kkkk..",
    ".kHhhdk..kdhhHk.",
    "kHkkkhdkkdhkkkHk",
    "khkHdkhkkhkdHkhk",
    "khkkhhdkkdhhkkhk",
    "kdhk.kk..kk.khdk",
    ".kdhk......khdk.",
    "..kdk......kdk..",
    "...k........k...",
    "................",
    "................",
    "................",
    "................",
]
HORN = {'.': T, 'k': H('#3f3324'), 'h': H('#d8c49c'), 'H': H('#f2e6c8'), 'd': H('#a78e66'), 'b': H('#6b4a2a')}


def build(shape, pal, marks=()):
    img = [[pal[ch] for ch in row] for row in shape]
    for x, y, ch in marks:
        img[y][x] = pal[ch]
    return img


def sheet(images, path, scale=8):
    w = len(images) * (16 * scale + 8)
    canvas = [[H('#3a3a3a')] * w for _ in range(16 * scale)]
    for k, img in enumerate(images):
        for y in range(16 * scale):
            for x in range(16 * scale):
                p = img[y // scale][x // scale]
                if p[3]:
                    canvas[y][k * (16 * scale + 8) + x] = p
    png(path, canvas)


if __name__ == "__main__":
    out = sys.argv[1]
    for shape in (GLOVE, BOTTLE_WIND, BOOTS, HORNS):
        assert len(shape) == 16 and all(len(r) == 16 for r in shape), [len(r) for r in shape]
    icons = {
        "silk_gloves": build(GLOVE, SILK, SILK_MARKS),
        "widow_gloves": build(GLOVE, WIDOW, WIDOW_MARKS),
        "breeze_bottle_overlay": build(BOTTLE_WIND, WIND),
        "magma_boots": build(BOOTS, MAGMA),
        "ram_horns": build(HORNS, HORN),
    }
    for name, img in icons.items():
        png(os.path.join(out, name + ".png"), img)
    previews = [icons["silk_gloves"], icons["widow_gloves"]]
    if len(sys.argv) > 2:
        from read_png import read_rgba  # noqa: E402
        base = read_rgba(sys.argv[2])
        over = icons["breeze_bottle_overlay"]
        previews.append([[over[y][x] if over[y][x][3] else base[y][x] for x in range(16)] for y in range(16)])
    previews += [icons["magma_boots"], icons["ram_horns"]]
    sheet(previews, os.path.join(out, "movilidad.png"))
    print("ok")
