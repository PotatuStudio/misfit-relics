"""Íconos 2D fieles al 3D: toma los renders de inventario que hace el visor (modo "Inventario", cuadro de escala 2,
32x32, el mismo ángulo que usa el juego) y los deja como texturas de ítem: alfa binario (sin bordes
semitransparentes). Así el ícono del inventario es el modelo 3D, plano.

Los renders se sacan del visor (botón Inventario, canvas #s2) para cada ítem y etapa, guardados como
<carpeta_renders>/<id>_32.png.

Uso: python gui_icons.py <carpeta_renders> <carpeta_salida>
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mclib import png  # noqa: E402
from read_png import read_rgba  # noqa: E402

IDS = [p + n for n in ("spud_cannon", "pressure_spud_cannon", "copper_trumpet")
       for p in ("", "exposed_", "weathered_", "oxidized_")]

if __name__ == "__main__":
    renders, out = sys.argv[1], sys.argv[2]
    for item_id in IDS:
        img = read_rgba(os.path.join(renders, item_id + "_32.png"))
        clean = [[(c[0], c[1], c[2], 255) if c[3] > 100 else (0, 0, 0, 0) for c in row] for row in img]
        png(os.path.join(out, item_id + "_icon.png"), clean)
    print("ok:", len(IDS), "íconos")
