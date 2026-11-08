"""Ícono 2D (16x16) del collar del murciélago, al estilo de los collares de Artifacts: cadena fina de plata en un
lazo y un dije pequeño (un rubí con dos alitas de murciélago). Sutil: el murciélago se insinúa, no domina.
Lleva el brillo animado de glint.py sobre la cadena y el rubí.

Salida: bat_collar_icon.png (+ .mcmeta) y bat_collar_icon_x16.png (vista previa del primer cuadro).
Uso: python collar_icon.py <carpeta_salida>
"""
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from glint import glint  # noqa: E402
from mclib import H, T, png  # noqa: E402

C = {
    '.': T,
    # cadena de plata: eslabón claro / oscuro
    'c': H('#e2e5ea'), 'C': H('#8d929c'),
    # argolla y contorno
    'i': H('#6a6e78'), 'k': H('#221f19'),
    # rubí (rojo de los ojos del murciélago): brillo, cuerpo, sombra
    'H': H('#ffb3a6'), 'R': H('#d8352a'), 'r': H('#7a1410'),
    # alitas: membrana oscura y borde
    'w': H('#3a2d3c'), 'W': H('#5a4258'),
}

ICON = [
    "................",
    ".....cCcCcC.....",
    "...Cc......cC...",
    "..c..........C..",
    "..C..........c..",
    "...c........C...",
    "....C......c....",
    ".....c....C.....",
    "......C..c......",
    ".......ii.......",
    ".......kk.......",
    "..wW..kHRk..Ww..",
    "..wwwkRRRrkwww..",
    "...wwwkRrkwww...",
    ".......kk.......",
    "................",
]
SHINY = set("cCiHRr")

if __name__ == "__main__":
    out = sys.argv[1]
    assert len(ICON) == 16 and all(len(r) == 16 for r in ICON), [len(r) for r in ICON]
    img = [[C[ch] for ch in row] for row in ICON]
    strip, meta = glint(img, lambda x, y: ICON[y][x] in SHINY)
    png(os.path.join(out, "bat_collar_icon.png"), strip)
    with open(os.path.join(out, "bat_collar_icon.png.mcmeta"), "w", encoding="utf-8", newline="\n") as f:
        json.dump(meta, f)
    big = [[img[y // 16][x // 16] if img[y // 16][x // 16][3] else (H('#8b8b8b') if (x // 64 + y // 64) % 2 else H('#c6c6c6'))
            for x in range(256)] for y in range(256)]
    png(os.path.join(out, "bat_collar_icon_x16.png"), big)
    print("ok")
