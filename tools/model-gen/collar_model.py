"""Modelo 3D del collar del murciélago: correa de cuero en anillo, argolla de hierro y un dije que es una placa
delgada con la silueta de un murciélago en pixel art (alas abiertas festoneadas, orejas, ojos rojos y colmillos),
recortada con transparencia. Colores del murciélago vanilla. Es la versión "puesta" que dibuja Trinkets; el ícono
del inventario es 2D (collar_icon.py).

Salida: collar_worn_strap.json, collar_worn_pendant.json, bat_collar_details.png
Uso: python collar_model.py <carpeta_salida>
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mclib import H, T, ModelBuilder, blank, coplanar_overlaps, cut, dump, png, repaint  # noqa: E402

OUT = sys.argv[1]
LEATHER = [H('#2a1a12'), H('#4a2e1e'), H('#6a432b'), H('#8a5a3a')]
IRON = [H('#2e3036'), H('#5e636e'), H('#9aa0aa'), H('#d6dae0')]
GEM = [H('#0b3c44'), H('#1f9aa0'), H('#5fd6cc'), H('#c8fff4')]

# Silueta del murciélago (16 x 10). Colores del murciélago vanilla:
#  O contorno  B pelaje  b pelaje claro  W membrana  w membrana clara  R ojos  F colmillos
BAT = [
    "......O..O......",
    ".....ObOObO.....",
    "O....OBbbBO....O",
    "OwO..ORBBRO..OwO",
    "OWwOOOBBBBOOOwWO",
    ".OWWWWOFFOWWWWO.",
    ".OWWWWOBBOWWWWO.",
    "..OWOWOBBOWOWO..",
    "...O.O.OO.O.O...",
    "................",
]
BAT_COLORS = {'O': H('#120e0c'), 'B': H('#26211f'), 'b': H('#43372f'), 'W': H('#5a3f58'), 'w': H('#825f7f'),
              'R': H('#d8352a'), 'F': H('#ece6d6')}


def make_texture():
    tex = blank()
    # Cuero, hierro y gema lisos: un solo color por material, así el anillo se ve continuo
    for x in range(16):
        for y in range(0, 4):
            tex[y][x] = LEATHER[2]
        tex[4][x] = IRON[2]
        tex[5][x] = GEM[1]
    # Silueta del murciélago (filas 7-15); la fila 6 queda transparente como margen para que el
    # hierro y la gema de arriba no se cuelen por el borde de la placa.
    for y, row in enumerate(BAT[:9]):
        for x, ch in enumerate(row):
            tex[7 + y][x] = BAT_COLORS.get(ch, T)
    return tex


UV = {'leather': [0, 0, 16, 4], 'iron': [0, 4, 16, 5], 'gem': [0, 5, 16, 6], 'bat': [0.02, 6.02, 15.98, 15.98]}


def pendant(m, cx, cy, cz, s=1.0):
    """Argolla + placa del murciélago (solo caras de frente y atrás, recortadas) + gema pequeña debajo."""
    m.box([cx - 0.55 * s, cy + 2.1 * s, cz - 0.3], [cx + 0.55 * s, cy + 3.0 * s, cz + 0.3], 'iron', name="argolla")
    w, h = 4.0 * s, 2.5 * s                               # la placa es 16x10 px de textura -> 8 x 5 unidades
    plate = {"name": "murcielago", "from": [round(cx - w, 3), round(cy - h, 3), round(cz - 0.1, 3)],
             "to": [round(cx + w, 3), round(cy + h, 3), round(cz + 0.1, 3)],
             "faces": {"south": {"uv": UV['bat'], "texture": "#t"}, "north": {"uv": UV['bat'], "texture": "#t"}}}
    m.elements.append(plate)
    g = 0.55 * s
    m.box([cx - g, cy - h - 0.4 * s - g, cz - 0.2], [cx + g, cy - h - 0.4 * s + g, cz + 0.2], 'gem', name="gema",
          rotation={"angle": 45, "axis": "z", "origin": [cx, cy - h - 0.4 * s, cz]})


def build_item():
    """Collar completo para el inventario y la mano: anillo de cuero con el murciélago colgando."""
    m = ModelBuilder(UV)
    cx, cy, cz, R, w = 8.0, 10.4, 8.0, 4.4, 0.5
    seg = R * math.tan(math.radians(22.5))
    m.box([cx - seg, cy + R - w, cz - 0.55], [cx + seg, cy + R + w, cz + 0.55], 'leather', name="correa_arriba")
    m.box([cx - R - w, cy - seg, cz - 0.55], [cx - R + w, cy + seg, cz + 0.55], 'leather', name="correa_izq")
    m.box([cx + R - w, cy - seg, cz - 0.55], [cx + R + w, cy + seg, cz + 0.55], 'leather', name="correa_der")
    d = R * math.cos(math.radians(45))
    for (sx, sy) in ((1, 1), (-1, 1), (1, -1), (-1, -1)):
        px, py = cx + sx * d, cy + sy * d
        m.box([px - seg, py - w, cz - 0.54], [px + seg, py + w, cz + 0.54], 'leather', name="correa_diag",
              rotation={"angle": -45 * sx * sy, "axis": "z", "origin": [px, py, cz]})
    pendant(m, cx, cy - R - 2.9, cz)
    return m.elements


def build_strap():
    """Correa puesta: anillo de cuero alrededor del cuello. Trinkets lo estira con scale_target xz_stretch, así que
    en X y Z el cuadro 0-16 equivale al ancho y la profundidad del torso de Steve (sea el modelo ancho o delgado);
    en Y una unidad es un píxel. El centro del modelo (y = 8) queda en el borde superior del torso."""
    m = ModelBuilder(UV)
    y0, y1 = 6.5, 8.0
    m.box([-0.8, y0, 16.0], [16.8, y1, 17.6], 'leather', name="correa_frente")
    m.box([-0.8, y0, -1.6], [16.8, y1, 0.0], 'leather', name="correa_atras")
    m.box([-0.8, y0, 0.0], [0.0, y1, 16.0], 'leather', name="correa_izq")
    m.box([16.0, y0, 0.0], [16.8, y1, 16.0], 'leather', name="correa_der")
    return m.elements


def build_pendant():
    """Dije puesto: argolla, placa del murciélago y gema. La parte de atrás queda en z = 8 (centro del modelo),
    que Trinkets pone sobre el pecho."""
    m = ModelBuilder(UV)
    pendant(m, 8.0, 8.0, 8.3)
    return m.elements


ITEM_DISPLAY = {
    "gui": {"rotation": [15, -25, 0], "translation": [0, 0.5, 0], "scale": [0.95, 0.95, 0.95]},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.5, 0.5, 0.5]},
    "fixed": {"rotation": [0, 180, 0], "translation": [0, 0, 0], "scale": [1, 1, 1]},
    "thirdperson_righthand": {"rotation": [0, 0, 0], "translation": [0, 2, 1], "scale": [0.55, 0.55, 0.55]},
    "thirdperson_lefthand": {"rotation": [0, 0, 0], "translation": [0, 2, 1], "scale": [0.55, 0.55, 0.55]},
    "firstperson_righthand": {"rotation": [0, -20, 0], "translation": [1, 3, 0], "scale": [0.6, 0.6, 0.6]},
    "firstperson_lefthand": {"rotation": [0, 20, 0], "translation": [1, 3, 0], "scale": [0.6, 0.6, 0.6]},
}


if __name__ == "__main__":
    from mclib import apply_vanilla
    strap, pend = build_strap(), build_pendant()
    apply_vanilla(strap + pend, UV, {'iron': 'iron'})
    t16 = make_texture()
    specials = {'bat': cut(t16, [0, 6, 16, 16])}
    painters = {'leather': LEATHER, 'gem': GEM}
    paint = repaint(strap + pend, UV, painters, specials, size=64)
    tex_ref = {"t": "misfitrelics:block/bat_collar_details", "iron": "minecraft:block/anvil", "particle": "#t"}
    dump({"textures": tex_ref, "elements": strap}, os.path.join(OUT, "collar_worn_strap.json"))
    dump({"textures": tex_ref, "elements": pend}, os.path.join(OUT, "collar_worn_pendant.json"))
    png(os.path.join(OUT, "bat_collar_details.png"), paint(painters, specials))
    print("correa:", len(strap), "piezas; dije:", len(pend), "piezas; solapadas:",
          len(coplanar_overlaps(strap)), len(coplanar_overlaps(pend)))
