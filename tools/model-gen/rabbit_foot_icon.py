"""Capa propia del ícono de la pata de conejo de la suerte: casquillo y argolla de oro sobre la pata vanilla.

El ícono del juego es item/generated con layer0 = minecraft:item/rabbit_foot (referenciada, no copiada) y
layer1 = esta capa. Para revisar, genera también una vista ampliada con la pata vanilla debajo.

Salida: lucky_/cursed_rabbit_foot_overlay.png (16x16) y sus vistas previas *_x16.png.
Uso: python rabbit_foot_icon.py <carpeta_salida> <rabbit_foot.png vanilla, solo para la vista previa>
"""
import os
import subprocess
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mclib import H, T, png  # noqa: E402

C = {'.': T, '1': H('#7a4a0c'), '2': H('#c08a1a'), '3': H('#ecc23e'), '4': H('#fff0a0')}
# Maldita: casquillo de hierro oscuro con brillo morado; la pata vanilla se tiñe con CURSED_TINT (en items/*.json).
CURSED = {'.': T, '1': H('#1e1826'), '2': H('#3d3150'), '3': H('#6b4f96'), '4': H('#c49bff')}
CURSED_TINT = 0x8A7A9E

OVERLAY = [
    "...........22...",
    "..........2..2..",
    "..........1342..",
    ".........13342..",
    ".........12221..",
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
]

def tint(c, rgb):
    if c[3] == 0:
        return c
    return (c[0] * (rgb >> 16 & 255) // 255, c[1] * (rgb >> 8 & 255) // 255, c[2] * (rgb & 255) // 255, c[3])


def preview(out, name, over, base):
    comp = [[over[y][x] if over[y][x][3] else base[y][x] for x in range(16)] for y in range(16)]
    big = [[comp[y // 16][x // 16] if comp[y // 16][x // 16][3]
            else (H('#8b8b8b') if (x // 64 + y // 64) % 2 else H('#c6c6c6')) for x in range(256)] for y in range(256)]
    png(os.path.join(out, name), big)


if __name__ == "__main__":
    out = sys.argv[1]
    assert len(OVERLAY) == 16 and all(len(r) == 16 for r in OVERLAY)
    over = [[C[ch] for ch in row] for row in OVERLAY]
    cursed = [[CURSED[ch] for ch in row] for row in OVERLAY]
    png(os.path.join(out, "lucky_rabbit_foot_overlay.png"), over)
    png(os.path.join(out, "cursed_rabbit_foot_overlay.png"), cursed)
    if len(sys.argv) > 2:
        from read_png import read_rgba  # noqa: E402
        base = read_rgba(sys.argv[2])
        preview(out, "lucky_rabbit_foot_x16.png", over, base)
        preview(out, "cursed_rabbit_foot_x16.png", cursed, [[tint(c, CURSED_TINT) for c in row] for row in base])
    print("ok")
