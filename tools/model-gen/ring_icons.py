"""Íconos 2D (16x16) del Anillo de la plaga y del Anillo del festín, al estilo de Rings of Ascension.
Misma silueta, distinto color y piedra, para que se lean como par.

Salida: plague_ring.png, feast_ring.png y sus vistas previas *_x16.png.
Uso: python ring_icons.py <carpeta_salida>
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mclib import H, T, png  # noqa: E402

# Estilo de los anillos de Rings of Ascension: aro redondo visto de frente, contorno oscuro, brillo arriba a la
# izquierda y sombra abajo a la derecha. Lo propio: aro de 10 px y una piedra engastada arriba que identifica cada
# anillo (verde de plaga / magenta de endermite).
#  a contorno  b brillo  c base  d sombra  G piedra  H brillo de la piedra
SHAPE = [
    "................",
    "................",
    ".......aa.......",
    ".....aaHGaa.....",
    "....abbGGcda....",
    "...abcaaaacda...",
    "...aca....ada...",
    "...aca....ada...",
    "...aca....ada...",
    "...aca....ada...",
    "...acdaaaadda...",
    "....adddddda....",
    ".....aaaaaa.....",
    "................",
    "................",
    "................",
]

OUTLINE = H('#221f19')
# Plaga: aro gris piedra (silverfish) con piedra verde enfermiza.
PLAGUE = {'.': T, 'a': OUTLINE, 'b': H('#e4e4e0'), 'c': H('#a9a9a4'), 'd': H('#6c6c68'),
          'G': H('#5fae2e'), 'H': H('#c8f59a')}
# Festín: aro morado oscuro (endermite) con piedra magenta.
FEAST = {'.': T, 'a': OUTLINE, 'b': H('#d79bff'), 'c': H('#8b4fc4'), 'd': H('#4a2670'),
         'G': H('#e03cc4'), 'H': H('#ffb8f0')}


def preview(img):
    return [[img[y // 16][x // 16] if img[y // 16][x // 16][3]
             else (H('#8b8b8b') if (x // 64 + y // 64) % 2 else H('#c6c6c6')) for x in range(256)] for y in range(256)]


if __name__ == "__main__":
    out = sys.argv[1]
    assert len(SHAPE) == 16 and all(len(r) == 16 for r in SHAPE), [len(r) for r in SHAPE]
    for name, pal in (("plague_ring", PLAGUE), ("feast_ring", FEAST)):
        img = [[pal[ch] for ch in row] for row in SHAPE]
        png(os.path.join(out, name + ".png"), img)
        png(os.path.join(out, name + "_x16.png"), preview(img))
    print("ok")
