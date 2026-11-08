"""Ícono 2D (16x16) del collar del murciélago: cordón delgado, argolla de hierro y el murciélago colgando.

Salida: bat_collar_icon.png (16x16) y bat_collar_icon_x16.png (ampliada para revisar).
Uso: python collar_icon.py <carpeta_salida>
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mclib import H, T, png  # noqa: E402

C = {
    '.': T,
    # cordón
    's': H('#4a3a30'),
    # hierro
    'I': H('#5e636e'), 'i': H('#d6dae0'),
    # murciélago (colores vanilla): contorno, pelaje, pelaje claro, membrana, membrana clara, ojos, colmillos
    'O': H('#120e0c'), 'B': H('#26211f'), 'b': H('#43372f'), 'W': H('#5a3f58'), 'w': H('#825f7f'),
    'R': H('#d8352a'), 'F': H('#ece6d6'),
}

ICON = [
    ".s............s.",
    "..s..........s..",
    "...s........s...",
    "....s......s....",
    ".....s....s.....",
    "......s..s......",
    ".......iI.......",
    "......OIIO......",
    ".....ObOObO.....",
    "O....OBbbBO....O",
    "OwO..ORBBRO..OwO",
    "OWwOOOBBBBOOOwWO",
    ".OWWWWOFFOWWWWO.",
    ".OWWWWOBBOWWWWO.",
    "..OWOWOBBOWOWO..",
    "...O.O.OO.O.O...",
]

if __name__ == "__main__":
    out = sys.argv[1]
    assert len(ICON) == 16 and all(len(r) == 16 for r in ICON), [len(r) for r in ICON]
    img = [[C[ch] for ch in row] for row in ICON]
    png(os.path.join(out, "bat_collar_icon.png"), img)
    big = [[img[y // 16][x // 16] if img[y // 16][x // 16][3] else (H('#8b8b8b') if (x // 64 + y // 64) % 2 else H('#c6c6c6'))
            for x in range(256)] for y in range(256)]
    png(os.path.join(out, "bat_collar_icon_x16.png"), big)
    print("ok")
