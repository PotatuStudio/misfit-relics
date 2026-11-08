"""Capa propia del ícono de la pata de conejo de la suerte: casquillo y argolla de oro sobre la pata vanilla.

El ícono del juego es item/generated con layer0 = minecraft:item/rabbit_foot (referenciada, no copiada) y
layer1 = esta capa. Para revisar, genera también una vista ampliada con la pata vanilla debajo.

Salida: lucky_rabbit_foot_overlay.png (16x16) y lucky_rabbit_foot_x16.png (vista previa).
Uso: python rabbit_foot_icon.py <carpeta_salida> <rabbit_foot.png vanilla, solo para la vista previa>
"""
import os
import subprocess
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mclib import H, T, png  # noqa: E402

C = {'.': T, '1': H('#7a4a0c'), '2': H('#c08a1a'), '3': H('#ecc23e'), '4': H('#fff0a0')}

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

if __name__ == "__main__":
    out = sys.argv[1]
    assert len(OVERLAY) == 16 and all(len(r) == 16 for r in OVERLAY)
    over = [[C[ch] for ch in row] for row in OVERLAY]
    png(os.path.join(out, "lucky_rabbit_foot_overlay.png"), over)
    if len(sys.argv) > 2:
        from read_png import read_rgba  # noqa: E402
        base = read_rgba(sys.argv[2])
        comp = [[over[y][x] if over[y][x][3] else base[y][x] for x in range(16)] for y in range(16)]
        big = [[comp[y // 16][x // 16] if comp[y // 16][x // 16][3]
                else (H('#8b8b8b') if (x // 64 + y // 64) % 2 else H('#c6c6c6')) for x in range(256)] for y in range(256)]
        png(os.path.join(out, "lucky_rabbit_foot_x16.png"), big)
    print("ok")
