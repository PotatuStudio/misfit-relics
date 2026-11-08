"""Modelo 3D del collar del murciélago: correa de cuero en anillo, argolla de hierro y un dije que es una placa
delgada con la silueta de un murciélago en pixel art (alas abiertas festoneadas, orejas, ojos rojos y colmillos),
recortada con transparencia. Colores del murciélago vanilla. Es la versión "puesta" que dibuja Trinkets; el ícono
del inventario es 2D (collar_icon.py).

Salida: collar_worn_pendant.json, bat_collar_details.png
Uso: python collar_model.py <carpeta_salida>
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mclib import H, T, ModelBuilder, blank, coplanar_overlaps, cut, dump, png, repaint  # noqa: E402

OUT = sys.argv[1]
LEATHER = [H('#2a1f1a'), H('#3a2c24'), H('#4a3a30'), H('#5c4a3e')]  # cordón oscuro, como en el ícono
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
# Un material por color del murciélago y del rubí. La UV solo identifica el material; repaint la reemplaza.
for _i, _ch in enumerate(BAT_COLORS):
    UV['bat_' + _ch] = [0.1 * _i, 15.0, 0.1 * _i + 0.05, 15.05]
UV.update({'ruby_dark': [2.0, 15.0, 2.05, 15.05], 'ruby': [2.1, 15.0, 2.15, 15.05],
           'ruby_light': [2.2, 15.0, 2.25, 15.05], 'ruby_shine': [2.3, 15.0, 2.35, 15.05]})


def ramp(hex_color):
    """Rampa para shade_face: el frente y los costados quedan con el color real, arriba más claro y abajo más oscuro."""
    r, g, b, _ = H(hex_color)

    def mix(f):
        if f < 1:
            return (int(r * f), int(g * f), int(b * f), 255)
        return (min(255, int(r + (255 - r) * (f - 1))), min(255, int(g + (255 - g) * (f - 1))),
                min(255, int(b + (255 - b) * (f - 1))), 255)
    return [mix(0.55), mix(0.75), mix(1.0), mix(1.25), mix(1.4)]


BAT_HEX = {'O': '#120e0c', 'B': '#26211f', 'b': '#43372f', 'W': '#5a3f58', 'w': '#825f7f', 'R': '#d8352a', 'F': '#ece6d6'}
# Rubí del rojo de los ojos del murciélago, en facetas: base oscura, cuerpo, cara frontal y un brillo.
RUBY_HEX = {'ruby_dark': '#7a1410', 'ruby': '#b3241c', 'ruby_light': '#d8352a', 'ruby_shine': '#ffb0a0'}


def bat_depth(ch, col):
    """Grosor (hacia adelante) de cada píxel: el cuerpo sobresale y su cara es plana (ojos y colmillos al ras);
    las alas son delgadas."""
    if 6 <= col <= 9:
        return 1.2
    if col in (5, 10):
        return 0.9
    return 0.5


def bat_relief(m, cx, cy, z0):
    """Murciélago en relieve, como los ítems vanilla extruidos: cada fila del sprite se agrupa en tramos del mismo
    color y grosor, y cada tramo es una caja. 1 píxel del sprite = 0,5 unidades."""
    for row, line in enumerate(BAT[:9]):
        col = 0
        while col < 16:
            ch = line[col]
            if ch == '.':
                col += 1
                continue
            depth = bat_depth(ch, col)
            end = col
            while end + 1 < 16 and line[end + 1] == ch and bat_depth(ch, end + 1) == depth:
                end += 1
            x0, x1 = cx - 4 + col * 0.5, cx - 4 + (end + 1) * 0.5
            y1 = cy + 2.5 - row * 0.5
            m.box([x0, y1 - 0.5, z0], [x1, y1, z0 + depth], 'bat_' + ch, name="murcielago")
            col = end + 1


def ruby(m, cx, cy, z0):
    """Rubí tallado: base en rombo, cuerpo, tabla frontal más clara y un punto de brillo arriba a la izquierda."""
    rot = {"angle": 45, "axis": "z", "origin": [cx, cy, z0]}
    m.box([cx - 1.0, cy - 1.0, z0], [cx + 1.0, cy + 1.0, z0 + 0.5], 'ruby_dark', name="rubi_base", rotation=rot)
    m.box([cx - 0.8, cy - 0.8, z0 + 0.5], [cx + 0.8, cy + 0.8, z0 + 0.9], 'ruby', name="rubi", rotation=rot)
    m.box([cx - 0.45, cy - 0.45, z0 + 0.9], [cx + 0.45, cy + 0.45, z0 + 1.15], 'ruby_light', name="rubi_tabla", rotation=rot)
    m.box([cx - 0.45, cy + 0.05, z0 + 1.15], [cx - 0.05, cy + 0.45, z0 + 1.25], 'ruby_shine', name="rubi_brillo", rotation=rot)


def pendant(m, cx, cy, cz, s=1.0):
    """Argolla + murciélago en relieve + rubí colgando debajo. La parte de atrás queda en z = cz - 0.3."""
    z0 = cz - 0.3
    m.box([cx - 0.45, cy + 2.1, z0 + 0.1], [cx + 0.45, cy + 3.0, z0 + 0.6], 'iron', name="argolla")
    bat_relief(m, cx, cy, z0)
    # El rubí cuelga pegado a las patas: su punta superior entra un poco en la última fila del murciélago.
    ruby(m, cx, cy - 2.0 - 1.15, z0 + 0.05)


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


def build_pendant():
    """Amuleto puesto, plano sobre el pecho: dos cordones en diagonal que bajan desde los lados del cuello hasta la
    argolla, y el murciélago colgando. La parte de atrás queda en z = 8 (centro del modelo), que Trinkets pone
    sobre el pecho. Con la escala 0,5 del render, 2 unidades = 1 píxel de Steve; el borde superior del torso
    queda en y = 14,4 (ver assets/misfitrelics/trinkets/bat_collar.json)."""
    m = ModelBuilder(UV)
    pendant(m, 8.0, 8.0, 8.3)
    top_y, ring_y = 14.3, 10.9
    for side in (-1, 1):
        x0, x1 = 8 + side * 5.0, 8 + side * 0.45           # del lado del cuello a la argolla
        cx, cy = (x0 + x1) / 2, (top_y + ring_y) / 2
        length = math.hypot(x0 - x1, top_y - ring_y)
        angle = math.degrees(math.atan2(top_y - ring_y, abs(x0 - x1)))
        m.box([round(cx - length / 2, 3), round(cy - 0.3, 3), 8.05], [round(cx + length / 2, 3), round(cy + 0.3, 3), 8.35],
              'leather', name="cordon",
              rotation={"angle": round(side * angle, 3), "axis": "z", "origin": [round(cx, 3), round(cy, 3), 8.2]})
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
    pend = build_pendant()
    apply_vanilla(pend, UV, {'iron': 'iron'})
    t16 = make_texture()
    specials = {}
    painters = {'leather': LEATHER}
    painters.update({'bat_' + ch: ramp(hx) for ch, hx in BAT_HEX.items()})
    painters.update({k: ramp(hx) for k, hx in RUBY_HEX.items()})
    paint = repaint(pend, UV, painters, specials, size=64)
    tex_ref = {"t": "misfitrelics:block/bat_collar_details", "iron": "minecraft:block/anvil", "particle": "#t"}
    dump({"textures": tex_ref, "elements": pend}, os.path.join(OUT, "collar_worn_pendant.json"))
    png(os.path.join(OUT, "bat_collar_details.png"), paint(painters, specials))
    print("amuleto puesto:", len(pend), "piezas; solapadas:", len(coplanar_overlaps(pend)))
