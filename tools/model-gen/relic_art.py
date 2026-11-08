"""Segunda ronda de arte de reliquias (íconos 16x16) y el ícono del mod.

- Guantes (seda, viuda, eléctrico): mano de cuatro dedos con pulgar, sombreado por columna y puño.
- Guantelete eléctrico y corazón de creeper cargado: rayos animados (cuadros con chispas que aparecen y se apagan)
  en vez del brillo de reflejo.
- Corazón de creeper cargado: corazón eléctrico celeste con la cara de creeper apenas insinuada.
- Botas de magma: capa de grietas de lava sobre las botas de cuero vanilla (teñidas en items/magma_boots.json).
- Concha de nautilus: capa con cadena de oro y gema del corazón del mar sobre la concha vanilla (teñida).
- Ícono del mod: panel de inventario vanilla con cuatro reliquias en sus espacios.

Salida: los .png (+ .mcmeta) en la carpeta indicada y la hoja arte.png para revisar.
Uso: python relic_art.py <carpeta_salida>
"""
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mclib import COPPER_STAGES, H, T, png  # noqa: E402
from read_png import read_rgba  # noqa: E402

GLOVE = [
    "................",
    "..kk.kk.kk.kk...",
    ".khakhakhakhak..",
    ".khakhakhakhak..",
    ".khakhakhakhak..",
    ".khasasasaaask..",
    ".khaaaaaaaaaskk.",
    ".khaaaaaaaaashak",
    ".khaaaaaaaaaaaak",
    "..khaaaaaaaasak.",
    "..khaaaaaaask...",
    "..kCCCCCCCCCCk..",
    "..kccccccccccck."[:16],
    "..kkkkkkkkkkkk..",
    "................",
    "................",
]
GLOVE[12] = "..kcccccccccck.."
GLOVE[11] = "..kCCCCCCCCCCk.."

SILK = {'.': T, 'k': H('#3a3a46'), 'h': H('#ffffff'), 'a': H('#e6e5ee'), 's': H('#b8b7c6'),
        'C': H('#c9c8d6'), 'c': H('#a6a5b6'), 'w': H('#8f8ea0')}
SILK_WEB = [(4, 6, 'w'), (5, 7, 'w'), (6, 8, 'w'), (8, 8, 'w'), (9, 7, 'w'), (10, 6, 'w'), (7, 6, 'w'), (7, 7, 'w'),
            (5, 9, 'w'), (9, 9, 'w')]
WIDOW = {'.': T, 'k': H('#08060c'), 'h': H('#3e3348'), 'a': H('#241c2c'), 's': H('#15101b'),
         'C': H('#3a2e44'), 'c': H('#281f30'), 'r': H('#d0212a'), 'R': H('#ff5a5a')}
WIDOW_MARK = [(6, 6, 'r'), (7, 6, 'R'), (8, 6, 'r'), (7, 7, 'r'), (6, 8, 'r'), (7, 8, 'r'), (8, 8, 'r')]
CU = COPPER_STAGES[""]
GAUNTLET = {'.': T, 'k': CU[0], 'h': CU[5], 'a': CU[3], 's': CU[1], 'C': CU[4], 'c': CU[2], 'p': CU[4], 'P': CU[2]}
# placas en los nudillos y una bobina de cobre en el puño
GAUNTLET_PLATES = [(3, 5, 'p'), (5, 5, 'p'), (8, 5, 'p'), (11, 5, 'p'), (4, 8, 'P'), (7, 8, 'P'), (10, 8, 'P')]

# Corazón eléctrico (silueta del corazón del HUD), celeste, con la cara de creeper apenas más oscura.
HEART = [
    "................",
    "...kkk....kkk...",
    "..kbHbk..kbHbk..",
    ".kbHHbbbbbbHHbk.",
    ".kbHffbbbbffbHk.",
    ".kbbffbbbbffbbk.",
    ".kbbbbbffbbbbbk.",
    "..kbbbffffbbbk..",
    "...kbbfbbfbbk...",
    "....kbbbbbbk....",
    ".....kbbbbk.....",
    "......kbbk......",
    ".......kk.......",
    "................",
    "................",
    "................",
]
HEART_P = {'.': T, 'k': H('#0b2a52'), 'b': H('#2f9be8'), 'H': H('#9be8ff'), 'f': H('#1f73b8')}

BOLT_CORE, BOLT_GLOW = H('#ffffff'), H('#7af0ff')


def bolt(points):
    """Chispa: la lista de píxeles (x, y); el primero y el último van en celeste, el resto en blanco."""
    return [(x, y, BOLT_GLOW if i in (0, len(points) - 1) else BOLT_CORE) for i, (x, y) in enumerate(points)]


GAUNTLET_BOLTS = [
    bolt([(14, 2), (15, 3), (14, 4), (15, 5)]),
    bolt([(0, 7), (1, 8), (0, 9), (1, 10)]),
    bolt([(4, 4), (5, 5), (6, 4), (7, 5)]),
    bolt([(14, 9), (15, 10), (14, 11)]),
    bolt([(8, 9), (9, 10), (10, 9), (11, 10)]),
]
HEART_BOLTS = [
    bolt([(13, 0), (14, 1), (15, 0)]),
    bolt([(0, 5), (1, 6), (0, 7), (1, 8)]),
    bolt([(4, 5), (5, 6), (6, 5), (7, 6), (8, 5)]),
    bolt([(14, 7), (15, 8), (14, 9)]),
    bolt([(7, 9), (8, 10), (9, 9)]),
]


def build(shape, pal, marks=()):
    img = [[pal[ch] for ch in row] for row in shape]
    for x, y, ch in marks:
        img[y][x] = pal[ch]
    return img


def crackle(img, bolts, path, name):
    """Textura animada: cuadros en los que aparece una chispa distinta y se apaga, con pausas entre medio."""
    frames = [img]
    for b in bolts:
        f = [row[:] for row in img]
        for x, y, col in b:
            f[y][x] = col
        frames.append(f)
    strip = [row for f in frames for row in f]
    order = []
    for i in range(1, len(frames)):
        order += [{"index": i, "time": 2}, {"index": 0, "time": 6 + (i * 5) % 9}]
    png(os.path.join(path, name + ".png"), strip)
    with open(os.path.join(path, name + ".png.mcmeta"), "w", encoding="utf-8", newline="\n") as fh:
        json.dump({"animation": {"frames": order}}, fh)


# Botas de magma: grietas de lava sobre la caña de las botas vanilla
BOOTS_CRACKS = {(4, 4): H('#ff9a2a'), (5, 5): H('#ffd04a'), (4, 7): H('#ff7a1a'), (5, 8): H('#ff9a2a'),
                (10, 5): H('#ff7a1a'), (11, 4): H('#ffd04a'), (11, 7): H('#ff9a2a'), (10, 8): H('#ff7a1a')}

# Concha de nautilus: cadena de oro arriba y gema del corazón del mar en el centro de la espiral
NAUTILUS_OVERLAY = {(6, 0): H('#c8961e'), (7, 0): H('#f0c84a'), (8, 0): H('#c8961e'), (5, 1): H('#f0c84a'),
                    (9, 1): H('#f0c84a'), (7, 1): H('#5a3a08'),
                    (8, 6): H('#2fb8d8'), (9, 6): H('#9ff0ff'), (8, 7): H('#1f7aa8'), (9, 7): H('#2fb8d8')}


def overlay(cells):
    img = [[T] * 16 for _ in range(16)]
    for (x, y), c in cells.items():
        img[y][x] = c
    return img


# --- Ícono del mod: panel de inventario vanilla con cuatro reliquias -------------------------------------------------
GUI = {'bg': H('#c6c6c6'), 'hi': H('#ffffff'), 'sh': H('#555555'), 'edge': H('#000000'),
       'slot': H('#8b8b8b'), 'slot_hi': H('#ffffff'), 'slot_sh': H('#373737')}


def panel(items):
    s = 64
    img = [[T] * s for _ in range(s)]
    for y in range(2, 62):
        for x in range(2, 62):
            img[y][x] = GUI['bg']
    for i in range(2, 62):
        img[2][i] = img[i][2] = GUI['edge']
        img[61][i] = img[i][61] = GUI['edge']
    for i in range(3, 60):
        img[3][i] = img[i][3] = GUI['hi']
        img[60][i] = img[i][60] = GUI['sh']
    origins = [(12, 12), (34, 12), (12, 34), (34, 34)]
    for (ox, oy), item in zip(origins, items):
        for y in range(18):
            for x in range(18):
                col = GUI['slot']
                if y == 0 or x == 0:
                    col = GUI['slot_sh']
                if y == 17 or x == 17:
                    col = GUI['slot_hi']
                img[oy + y][ox + x] = col
        for y in range(16):
            for x in range(16):
                if item[y][x][3]:
                    img[oy + 1 + y][ox + 1 + x] = item[y][x]
    return img


def upscale(img, k):
    return [[img[y // k][x // k] for x in range(len(img[0]) * k)] for y in range(len(img) * k)]


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
    for shape in (GLOVE, HEART):
        assert len(shape) == 16 and all(len(r) == 16 for r in shape), [len(r) for r in shape]
    silk = build(GLOVE, SILK, SILK_WEB)
    widow = build(GLOVE, WIDOW, WIDOW_MARK)
    gauntlet = build(GLOVE, GAUNTLET, GAUNTLET_PLATES)
    heart = build(HEART, HEART_P)
    png(os.path.join(out, "silk_gloves.png"), silk)
    png(os.path.join(out, "widow_gloves.png"), widow)
    crackle(gauntlet, GAUNTLET_BOLTS, out, "electric_gauntlet")
    crackle(heart, HEART_BOLTS, out, "charged_creeper_heart")
    png(os.path.join(out, "magma_boots_overlay.png"), overlay(BOOTS_CRACKS))
    png(os.path.join(out, "nautilus_shell_overlay.png"), overlay(NAUTILUS_OVERLAY))

    collar = read_rgba(os.path.join(out, "bat_collar_icon.png"))[:16]
    ring = read_rgba(os.path.join(out, "berserker_ring.png"))[:16]
    icon = panel([collar, ring, gauntlet, heart])
    png(os.path.join(out, "icon.png"), upscale(icon, 2))
    png(os.path.join(out, "relic_icon.png"), collar)

    # Vista previa: guantes, guantelete con chispa, corazón con chispa, y el ícono
    def with_bolt(img, b):
        f = [row[:] for row in img]
        for x, y, c in b:
            f[y][x] = c
        return f
    sheet([silk, widow, with_bolt(gauntlet, GAUNTLET_BOLTS[0]), with_bolt(heart, HEART_BOLTS[2])],
          os.path.join(out, "arte.png"))
    png(os.path.join(out, "icono_preview.png"), upscale(icon, 2))
    print("ok")
