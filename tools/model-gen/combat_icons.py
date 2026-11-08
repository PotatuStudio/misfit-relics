"""Íconos 2D (16x16) de las reliquias de combate: guantelete eléctrico, corazón de creeper cargado, carcaj del bosque,
carcaj del vigilante, concha de nautilus (capa sobre la concha vanilla) y anillo del berserker.
Los de metal o energía llevan el brillo animado de glint.py.

Salida: <id>.png (+ .mcmeta si brilla) y la hoja combate.png para revisar.
Uso: python combat_icons.py <carpeta_salida> [<nautilus_shell.png vanilla, para la vista previa>]
"""
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from glint import glint  # noqa: E402
from mclib import COPPER_STAGES, H, T, png  # noqa: E402
from mobility_icons import GLOVE  # noqa: E402
from ring_icons import SHAPE as RING  # noqa: E402

CU = COPPER_STAGES[""]
GAUNTLET = {'.': T, 'k': CU[0], 'a': CU[3], 'b': CU[2], 'B': CU[4], 'w': CU[5], 'z': H('#fff27a'), 'Z': H('#7af0ff')}
GAUNTLET_MARKS = [(5, 5, 'w'), (6, 6, 'w'), (8, 5, 'w'), (12, 1, 'z'), (13, 2, 'Z'), (12, 3, 'z'), (1, 8, 'Z'),
                  (2, 9, 'z'), (13, 9, 'Z'), (14, 8, 'z')]

# Silueta del corazón del HUD de Minecraft, en verde de creeper, con su cara en verde oscuro (sutil) y unas
# chispas celestes de creeper cargado.
HEART = [
    "................",
    "...kkk....kkk...",
    "..kgGgk..kgGgk..",
    ".kgGGggggggGGgk.",
    ".kgGddggggddgGk.",
    ".kggddggggddggk.",
    ".kgggggddgggggk.",
    "..kgggddddgggk..",
    "...kggdggdggk...",
    "....kggggggk....",
    ".....kggggk.....",
    "......kggk......",
    ".......kk.......",
    "................",
    "................",
    "................",
]
HEART_P = {'.': T, 'k': H('#1f3d18'), 'g': H('#5aa646'), 'G': H('#8fd06a'), 'd': H('#2e5a24'),
           'c': H('#8fe8ff'), 'C': H('#d8f8ff')}
HEART_SPARKS = [(14, 1, 'C'), (15, 2, 'c'), (0, 6, 'c'), (13, 10, 'c')]


def quiver(body, strap, extra):
    """Carcaj en diagonal (abajo a la izquierda -> arriba a la derecha) con plumas de flecha asomando arriba."""
    img = [[T] * 16 for _ in range(16)]
    for y in range(16):
        for x in range(16):
            a, c = x - y, x + y
            if -9 <= a <= 5 and 13 <= c <= 18:
                k = c - 13
                col = body[0] if k in (0, 5) or a in (-9, 5) else body[[3, 2, 2, 1][min(3, k - 1)]]
                img[y][x] = col
    # correa en diagonal
    for x, y in ((4, 11), (5, 10), (6, 9), (7, 8), (8, 7)):
        if img[y][x][3]:
            img[y][x] = strap
    # plumas de las flechas asomando por la boca (arriba a la derecha)
    # tres flechas que salen de la boca hacia arriba a la derecha: astil de madera y plumas blancas
    shaft, fletch, fletch2 = H('#8a5a2a'), H('#f2f2f2'), H('#c8c8c8')
    for mx, my in ((9, 4), (10, 5), (11, 6)):
        img[my - 1][mx + 1] = shaft
        img[my - 2][mx + 2] = shaft
        img[my - 3][mx + 3] = fletch
        if my - 3 >= 0 and mx + 4 < 16:
            img[my - 3][mx + 4] = fletch2
    for x, y, col in extra:
        img[y][x] = col
    return img


LEATHER = [H('#2e1d10'), H('#5a3a20'), H('#7a5030'), H('#9a6a40')]
FOREST_EXTRA = [(5, 12, H('#2f6b1f')), (6, 11, H('#4f9a2f')), (7, 12, H('#2f6b1f')), (8, 9, H('#4f9a2f')),
                (9, 8, H('#2f6b1f')), (10, 9, H('#4f9a2f')), (10, 6, H('#4f9a2f')), (11, 5, H('#2f6b1f')),
                (6, 13, H('#c8e070')), (9, 10, H('#c8e070')), (11, 7, H('#c8e070')), (4, 12, H('#8a1a1a'))]
DARK = [H('#120f1a'), H('#2a2238'), H('#3a3050'), H('#4c4066')]
WATCHER_EXTRA = [(7, 9, H('#e8f6ff')), (8, 9, H('#2fd0c8')), (8, 8, H('#e8f6ff')), (9, 8, H('#e8f6ff')),
                 (7, 10, H('#e8f6ff')), (8, 10, H('#0d3a38'))]

NAUTILUS_CORD = [
    ".....ss.ss......",
    "....s.....s.....",
    "....s.....s.....",
    ".....s...s......",
    "......sGs.......",
] + ["." * 16] * 11
CORD = {'.': T, 's': H('#4a3828'), 'G': H('#e8c84a')}

BERSERKER = {'.': T, 'a': H('#221f19'), 'b': H('#8a8f99'), 'c': H('#5a5e66'), 'd': H('#34363c'),
             'G': H('#c4161c'), 'H': H('#ff8a80')}


def build(shape, pal, marks=()):
    img = [[pal[ch] for ch in row] for row in shape]
    for x, y, ch in marks:
        img[y][x] = pal[ch]
    return img


def save(out, name, img, shiny=None):
    if shiny:
        strip, meta = glint(img, shiny)
        png(os.path.join(out, name + ".png"), strip)
        with open(os.path.join(out, name + ".png.mcmeta"), "w", encoding="utf-8", newline="\n") as f:
            json.dump(meta, f)
    else:
        png(os.path.join(out, name + ".png"), img)


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
    for shape in (HEART, NAUTILUS_CORD):
        assert len(shape) == 16 and all(len(r) == 16 for r in shape), [len(r) for r in shape]
    gauntlet = build(GLOVE, GAUNTLET, GAUNTLET_MARKS)
    heart = build(HEART, HEART_P, HEART_SPARKS)
    forest = quiver(LEATHER, H('#3a2414'), FOREST_EXTRA)
    watcher = quiver(DARK, H('#5a4a7a'), WATCHER_EXTRA)
    cord = build(NAUTILUS_CORD, CORD)
    ring = build(RING, BERSERKER)
    save(out, "electric_gauntlet", gauntlet, lambda x, y: gauntlet[y][x][3] and GLOVE[y][x] != '.')
    save(out, "charged_creeper_heart", heart, lambda x, y: HEART[y][x] in "gG")
    save(out, "forest_quiver", forest)
    save(out, "watcher_quiver", watcher)
    save(out, "nautilus_shell_overlay", cord)
    save(out, "berserker_ring", ring, lambda x, y: RING[y][x] in "bcdGH")
    previews = [gauntlet, heart, forest, watcher]
    if len(sys.argv) > 2:
        from read_png import read_rgba  # noqa: E402
        base = read_rgba(sys.argv[2])
        previews.append([[cord[y][x] if cord[y][x][3] else base[y][x] for x in range(16)] for y in range(16)])
    previews.append(ring)
    sheet(previews, os.path.join(out, "combate.png"))
    print("ok")
