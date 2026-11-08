"""Ícono del mod (128x128, pixel art 32x32 ampliado x4) y el ícono de la pestaña creativa (16x16).

Medallón de reliquia: marco de oro con cuatro gemas de rareza (común, poco común, rara, épica), fondo morado con
estrellas y, al centro, la papa venenosa de PotatuStudio engastada en oro como un amuleto que brilla.

Salida: icon.png (128) y relic_icon.png (16) + vista previa icono_preview.png
Uso: python mod_icon.py <carpeta_salida>
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mclib import H, T, png  # noqa: E402

GOLD = [H('#5a3a08'), H('#8a6010'), H('#c8961e'), H('#f0c84a'), H('#fff0a0')]
BG = [H('#1a0f2e'), H('#2a1848'), H('#3a2462')]
POTATO = [H('#2e3a12'), H('#4f6a1e'), H('#7a9a2c'), H('#a8c84a'), H('#d8f080')]
GEMS = [H('#e8e8e8'), H('#5fd34a'), H('#3fa0ff'), H('#c05cff')]
OUTLINE = H('#0c0614')


def potato_amulet(size, cx, cy, rx, ry, setting=True):
    """Papa (elipse sombreada, luz arriba a la izquierda) con engaste de oro y argolla. Devuelve dict (x,y)->color."""
    px = {}
    for y in range(size):
        for x in range(size):
            dx, dy = (x + 0.5 - cx) / rx, (y + 0.5 - cy) / ry
            d = dx * dx + dy * dy
            if d <= 1.0:
                light = -(dx * 0.6 + dy * 0.8)          # luz desde arriba a la izquierda
                k = 2 + light * 1.6 - d * 0.8
                px[(x, y)] = POTATO[max(0, min(4, int(round(k))))]
            elif setting and d <= 1.45:
                ang = math.atan2(dy, dx)
                k = 2 + (-math.sin(ang) - math.cos(ang)) * 0.9
                px[(x, y)] = GOLD[max(1, min(4, int(round(k))))]
    # ojos de la papa (manchas oscuras), fijos, no al azar
    for ex, ey in ((cx - rx * 0.35, cy - ry * 0.1), (cx + rx * 0.3, cy + ry * 0.35), (cx - rx * 0.05, cy + ry * 0.55)):
        q = (int(ex), int(ey))
        if q in px and px[q] in POTATO:
            px[q] = POTATO[0]
    return px


def medallion():
    s = 32
    img = [[T] * s for _ in range(s)]
    c = 15.5
    for y in range(s):
        for x in range(s):
            r = math.hypot(x + 0.5 - 16, y + 0.5 - 16)
            if r <= 15.5:
                if r > 13.0:
                    ang = math.atan2(y + 0.5 - 16, x + 0.5 - 16)
                    k = 2 + (-math.sin(ang) - math.cos(ang)) * 1.0
                    img[y][x] = GOLD[max(1, min(4, int(round(k))))]
                    if r > 15.0 or r < 13.5:
                        img[y][x] = GOLD[0]
                else:
                    img[y][x] = BG[0] if r > 11 else (BG[1] if r > 6 else BG[2])
    # estrellas fijas
    for x, y in ((8, 9), (23, 8), (6, 18), (25, 20), (11, 25), (21, 25), (16, 5)):
        img[y][x] = H('#e8d8ff')
    # gemas de rareza en los cuatro puntos cardinales del marco
    for (x, y), g in zip(((16, 1), (30, 16), (16, 30), (1, 16)), GEMS):
        for dx, dy in ((0, 0), (-1, 0), (0, -1), (-1, -1)):
            img[y + dy][x + dx] = g
        img[y - 1][x - 1] = H('#ffffff')
    # brillo verde detrás de la papa
    for y in range(s):
        for x in range(s):
            if 4.5 < math.hypot(x + 0.5 - 16, y + 0.5 - 17.5) <= 7.5 and img[y][x] in BG:
                img[y][x] = H('#24402a') if (x + y) % 2 else BG[2]
    # argolla del amuleto
    for x, y in ((15, 8), (16, 8), (14, 9), (17, 9), (14, 10), (17, 10)):
        img[y][x] = GOLD[3]
    for (x, y), col in potato_amulet(s, 16, 17.5, 4.6, 5.4).items():
        img[y][x] = col
    return img


def tab_icon():
    s = 16
    img = [[T] * s for _ in range(s)]
    for x, y in ((7, 1), (8, 1), (6, 2), (9, 2), (6, 3), (9, 3)):
        img[y][x] = GOLD[3]
    for (x, y), col in potato_amulet(s, 8, 9.5, 4.2, 4.9).items():
        img[y][x] = col
    # contorno oscuro para que se lea sobre el fondo del menú
    out = [row[:] for row in img]
    for y in range(s):
        for x in range(s):
            if img[y][x][3] == 0 and any(0 <= x + dx < s and 0 <= y + dy < s and img[y + dy][x + dx][3]
                                         for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                out[y][x] = OUTLINE
    return out


def upscale(img, k):
    return [[img[y // k][x // k] for x in range(len(img[0]) * k)] for y in range(len(img) * k)]


if __name__ == "__main__":
    out = sys.argv[1]
    med = medallion()
    png(os.path.join(out, "icon.png"), upscale(med, 4))
    tab = tab_icon()
    png(os.path.join(out, "relic_icon.png"), tab)
    prev = upscale(med, 4)
    tab8 = upscale(tab, 8)
    canvas = [[H('#3a3a3a')] * (128 + 16 + 128) for _ in range(128)]
    for y in range(128):
        for x in range(128):
            if prev[y][x][3]:
                canvas[y][x] = prev[y][x]
            if tab8[y][x][3]:
                canvas[y][144 + x] = tab8[y][x]
    png(os.path.join(out, "icono_preview.png"), canvas)
    print("ok")
