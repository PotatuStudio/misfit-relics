"""Diseño alternativo "rústico" del cañón de papas: armado a mano con las piezas de la receta.

Tramos de tubo de cobre desalineados, unidos con amarras de cuerda; un bloque de pistón atrás
(lo que empuja la papa); gatillo de pedernal y empuñadura de palo.

Salida: rustic_base.json, rustic_base_charged.json, rustic_<etapa>_model.png
Uso: python rustic_model.py <carpeta_salida>
"""
import json
import os
import struct
import sys
import zlib

OUT = sys.argv[1]


def H(s):
    return tuple(int(s[i:i + 2], 16) for i in (1, 3, 5)) + (255,)


T = (0, 0, 0, 0)
STONE = [H('#4a4a4a'), H('#686868'), H('#7f7f7f'), H('#9a9a9a')]          # piedra del pistón
PLANK = [H('#5e4a2c'), H('#7d6239'), H('#9c7f4e'), H('#b8945f')]          # madera del pistón
STICK = [H('#3b2a14'), H('#5a3f1e'), H('#76552b'), H('#8f6a38')]          # palo
ROPE = [H('#8a8270'), H('#b3ab95'), H('#d8d1bb'), H('#f0ead6')]           # cuerda
FLINT = [H('#1e1e22'), H('#34343a'), H('#4e4e56'), H('#707078')]          # pedernal
K = H('#0e0806')
P = [H('#9a5500'), H('#c8973a'), H('#e9ba62'), H('#f8d086')]
STAGES = {
    'spud_cannon': [H('#421c11'), H('#5f2c1c'), H('#9c4e31'), H('#c15a36'), H('#e3826c'), H('#fc9982')],
    'exposed_spud_cannon': [H('#3e3229'), H('#5e4c3f'), H('#796454'), H('#947661'), H('#ba8277'), H('#ce8d83')],
    'weathered_spud_cannon': [H('#26382f'), H('#3e5446'), H('#497164'), H('#6c975c'), H('#66a977'), H('#7ab799')],
    'oxidized_spud_cannon': [H('#1c3a30'), H('#2a5244'), H('#3b6655'), H('#3e816b'), H('#4fab90'), H('#6ec59f')],
}


def make_texture(BR):
    tex = [[T] * 16 for _ in range(16)]

    def fill(x0, y0, x1, y1, c):
        for y in range(y0, y1):
            for x in range(x0, x1):
                tex[y][x] = c

    # Tubo de cobre (filas 0-3): abolladuras y soldaduras irregulares
    for x in range(16):
        tex[0][x] = BR[5] if x in (2, 9) else BR[4]
        tex[1][x] = BR[2] if x in (5, 12) else BR[3]
        tex[2][x] = BR[3] if x % 3 else BR[2]
        tex[3][x] = BR[1] if x in (4, 10, 14) else BR[2]
    # Piedra del pistón (0..8, 4..8): adoquín
    stone = ["2312", "1023", "3201", "2130"]
    for y in range(4):
        for x in range(8):
            tex[4 + y][x] = STONE[int(stone[y][x % 4])]
    # Madera del pistón (8..16, 4..8): tablas con junta
    for y in range(4):
        for x in range(8, 16):
            tex[4 + y][x] = PLANK[3 if y == 0 else 2 if y < 3 else 1]
        tex[4 + y][11] = PLANK[0]
    # Palo (0..8, 8..12)
    for y in range(4):
        for x in range(8):
            tex[8 + y][x] = STICK[3 - y]
    # Cuerda (8..12, 8..12): trenzado diagonal
    for y in range(4):
        for x in range(8, 12):
            tex[8 + y][x] = ROPE[3] if (x + y) % 3 == 0 else ROPE[1] if (x + y) % 3 == 1 else ROPE[2]
    # Pedernal (12..16, 8..12)
    for y in range(4):
        for x in range(12, 16):
            tex[8 + y][x] = FLINT[3] if (x == 12 and y < 2) else FLINT[1 + (x + y) % 2]
    # Boca del tubo (0..4, 12..16)
    fill(0, 12, 4, 16, BR[2])
    fill(1, 13, 3, 15, K)
    tex[12][0] = BR[4]
    tex[12][1] = BR[4]
    # Cara del pistón (4..8, 12..16): madera con borde
    fill(4, 12, 8, 16, PLANK[2])
    for (x, y) in ((4, 12), (7, 12), (4, 15), (7, 15)):
        tex[y][x] = PLANK[0]
    tex[13][5] = PLANK[3]
    # Papa (8..12, 12..16)
    fill(8, 12, 12, 16, P[2])
    tex[12][8] = P[3]
    tex[12][9] = P[3]
    tex[13][10] = P[0]
    tex[15][9] = P[0]
    tex[14][11] = P[1]
    tex[15][11] = P[1]
    # Abertura (12..16, 12..16)
    fill(12, 12, 16, 16, K)
    return tex


def png(path, img):
    raw = b"".join(b"\x00" + b"".join(bytes(c) for c in row) for row in img)

    def chunk(tag, data):
        return struct.pack(">I", len(data)) + tag + data + struct.pack(">I", zlib.crc32(tag + data) & 0xffffffff)

    with open(path, "wb") as f:
        f.write(b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", 16, 16, 8, 6, 0, 0, 0))
                + chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b""))


UV = {'copper': [0, 0, 16, 4], 'stone': [0, 4, 8, 8], 'plank': [8, 4, 16, 8], 'stick': [0, 8, 8, 12],
      'rope': [8, 8, 12, 12], 'flint': [12, 8, 16, 12], 'muzzle': [0, 12, 4, 16], 'pistonface': [4, 12, 8, 16],
      'potato': [8, 12, 12, 16], 'bore': [12, 12, 16, 16]}


def faces(side, east=None, west=None, up=None):
    f = {k: {"uv": UV[side], "texture": "#t"} for k in ('north', 'south', 'up', 'down')}
    if up:
        f['up'] = {"uv": UV[up], "texture": "#t"}
    f['east'] = {"uv": UV[east or side], "texture": "#t"}
    f['west'] = {"uv": UV[west or side], "texture": "#t"}
    return f


def box(frm, to, side, east=None, west=None, up=None, name=""):
    return {"name": name, "from": [round(v, 3) for v in frm], "to": [round(v, 3) for v in to],
            "faces": faces(side, east, west, up)}


def cyl(x0, x1, r, side, east=None, west=None, name="", cy=8.0, cz=8.0):
    """Cilindro octogonal a lo largo de X con centro (cy, cz); extremos escalonados contra z-fighting."""
    t, q, e = r * 0.414, r * 0.707, 0.04
    return [box([x0, cy - r, cz - t], [x1, cy + r, cz + t], side, east, west, name=name + "_v"),
            box([x0 - e, cy - t, cz - r], [x1 + e, cy + t, cz + r], side, east, west, name=name + "_h"),
            box([x0 - 2 * e, cy - q, cz - q], [x1 + 2 * e, cy + q, cz + q], side, east, west, name=name + "_c")]


def build(loaded=False):
    els = []
    # Bloque de pistón atrás: costados de piedra, trasera de tablas, cara de madera hacia el tubo
    piston = box([0.8, 5.45, 5.45], [5.3, 10.55, 10.55], 'stone', east='pistonface', west='plank', name="piston")
    els.append(piston)
    els.append(box([0.6, 5.25, 5.25], [1.65, 10.75, 10.75], 'plank', name="piston_tapa"))       # tapa de madera trasera
    # Tramos de tubo de cobre, desalineados a propósito (armado a mano)
    els += cyl(5.1, 9.0, 2.1, 'copper', name="tubo1", cy=8.0)
    els += cyl(8.8, 12.9, 1.9, 'copper', name="tubo2", cy=8.25, cz=7.9)
    els += cyl(12.7, 15.6, 2.2, 'copper', east='muzzle', name="tubo3", cy=8.1)
    els.append(box([15.55, 7.0, 7.0], [15.65, 9.2, 9.2], 'bore', name="abertura"))
    # Amarras de cuerda en las uniones
    els += cyl(8.4, 9.35, 2.4, 'rope', name="amarra1", cy=8.1)
    els += cyl(12.3, 13.2, 2.45, 'rope', name="amarra2", cy=8.15)
    # Empuñadura de palo, amarrada al pistón, y gatillo de pedernal
    els.append(box([2.35, 1.0, 7.35], [3.65, 5.5, 8.65], 'stick', name="empunadura"))
    els.append(box([2.15, 4.4, 7.15], [3.85, 5.3, 8.85], 'rope', name="amarra_empunadura"))
    els.append(box([4.1, 3.9, 7.55], [5.1, 5.0, 8.45], 'flint', name="gatillo"))
    if loaded:
        # Papa ovalada asomando por la boca (óvalo a lo largo del tubo, extremos redondeados)
        els += cyl(15.0, 16.8, 1.25, 'potato', name="papa", cy=8.1)
        els += cyl(14.7, 17.1, 0.8, 'potato', name="papa_punta", cy=8.15)
    return els


DISPLAY = {
    "gui": {"rotation": [25, -30, 35], "translation": [0, 0, 0], "scale": [0.85, 0.85, 0.85]},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.5, 0.5, 0.5]},
    "fixed": {"rotation": [0, 90, 0], "translation": [0, 0, 0], "scale": [0.9, 0.9, 0.9]},
    "thirdperson_righthand": {"rotation": [0, 90, 180], "translation": [0, 2, 1], "scale": [0.8, 0.8, 0.8]},
    "thirdperson_lefthand": {"rotation": [0, -90, 180], "translation": [0, 2, 1], "scale": [0.8, 0.8, 0.8]},
    "firstperson_righthand": {"rotation": [0, 90, -5], "translation": [1.5, 3, 0], "scale": [0.7, 0.7, 0.7]},
    "firstperson_lefthand": {"rotation": [0, -90, 5], "translation": [1.5, 3, 0], "scale": [0.7, 0.7, 0.7]},
    "head": {"rotation": [0, -90, 0], "translation": [0, 10, 0], "scale": [1, 1, 1]},
}


def dump(obj, name):
    with open(os.path.join(OUT, name), "w", encoding="utf-8") as f:
        json.dump(obj, f, indent=1)


def model(**kw):
    return {"textures": {"t": "misfitrelics:item/rustic_spud_cannon_model", "particle": "#t"},
            "gui_light": "side", "elements": build(**kw), "display": DISPLAY}


sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import mclib  # noqa: E402  (texturizado por cara compartido)


def _stone(w, h, kind):
    """Piedra del pistón: adoquines con juntas (patrón fijo) y luz desde arriba."""
    img = []
    for y in range(h):
        row = []
        for x in range(w):
            joint = y % 3 == 2 or (x + (y // 3) * 2) % 4 == 3
            c = STONE[0] if joint else (STONE[3] if y == 0 else STONE[2])
            row.append(STONE[1] if kind == 'down' and not joint else c)
        img.append(row)
    return img


def _plank(w, h, kind):
    """Tablas: una junta oscura cada 3 filas, desplazada por tabla."""
    img = []
    for y in range(h):
        row = []
        for x in range(w):
            seam = y % 3 == 2 or (y // 3 % 2 == 0 and x % 5 == 4) or (y // 3 % 2 == 1 and x % 5 == 1)
            row.append(PLANK[0] if seam else (PLANK[3] if y % 3 == 0 else PLANK[2]))
        img.append(row)
    return img


def _rope(w, h, kind):
    """Cuerda trenzada: diagonales alternadas."""
    return [[ROPE[3] if (x + y) % 3 == 0 else ROPE[2] if (x + y) % 3 == 1 else ROPE[1] for x in range(w)]
            for y in range(h)]


_charged = [e for e in build(loaded=True) if e["name"] != "piston_tapa"]   # el pistón vanilla ya trae su fondo
_base = [e for e in _charged if not e["name"].startswith("papa")]
mclib.apply_vanilla(_charged, UV,
                    {'copper': 'copper', 'muzzle': 'copper', 'stone': 'piston_side', 'pistonface': 'piston_top',
                     'plank': 'piston_bottom', 'stick': 'stick', 'rope': 'rope', 'flint': 'flint'},
                    rotations={'stone': {'north': 270, 'south': 90, 'up': 90, 'down': 90}})
_t0 = make_texture(STAGES['spud_cannon'])
_spec = {'bore': mclib.cut(_t0, UV['bore'])}
_paint = mclib.repaint(_charged, UV, {'potato': P}, _spec, size=16)
mclib.png(os.path.join(OUT, "spud_cannon_details.png"), _paint({'potato': P}, _spec))
COPPER_BY_STAGE = {'spud_cannon': 'minecraft:block/copper_block', 'exposed_spud_cannon': 'minecraft:block/exposed_copper', 'weathered_spud_cannon': 'minecraft:block/weathered_copper', 'oxidized_spud_cannon': 'minecraft:block/oxidized_copper'}
_tex = {"t": "misfitrelics:block/spud_cannon_details", "copper": "minecraft:block/copper_block",
        "piston_side": "minecraft:block/piston_side", "piston_top": "minecraft:block/piston_top",
        "piston_bottom": "minecraft:block/piston_bottom", "stick": "minecraft:block/stripped_oak_log",
        "rope": "minecraft:block/white_wool", "flint": "minecraft:block/blackstone", "particle": "#copper"}
for _name, _els in (("rustic_base.json", _base), ("rustic_base_charged.json", _charged)):
    dump({"textures": _tex, "gui_light": "side", "elements": _els, "display": DISPLAY}, _name)
mclib.dump(COPPER_BY_STAGE, os.path.join(OUT, "rustic_stages.json"))
print("ok", len(_base), "elementos; solapadas:", len(mclib.coplanar_overlaps(_charged)))
