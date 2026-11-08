"""Íconos 2D (16x16) del Anillo de la plaga (hierro con un silverfish enroscado) y del Anillo del festín (oro con
un endermite). Misma silueta, distinto material y bicho, para que se lean como par.

Salida: plague_ring.png, feast_ring.png y sus vistas previas *_x16.png.
Uso: python ring_icons.py <carpeta_salida>
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mclib import H, T, png  # noqa: E402

SHAPE = [
    "................",
    "................",
    "......s.s.s.....",
    "....slSlSlSs....",
    "...esSSSSSSSs...",
    "....ssssssss....",
    "....bBBiBBBb....",
    "...bB......Bb...",
    "..bB........Bb..",
    "..B..........B..",
    "..B..........B..",
    "..bB........Bb..",
    "...bB......Bb...",
    "....bbBBBBbb....",
    "................",
    "................",
]

PLAGUE = {'.': T, 'b': H('#5e636e'), 'B': H('#9aa0aa'), 'i': H('#d6dae0'),
          's': H('#4f4f4f'), 'S': H('#8c8c8c'), 'l': H('#b8b8b8'), 'e': H('#141414')}
FEAST = {'.': T, 'b': H('#8a5a12'), 'B': H('#e0b030'), 'i': H('#fff0a0'),
         's': H('#1c1226'), 'S': H('#3a2550'), 'l': H('#8f5fc7'), 'e': H('#d070ff')}


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
