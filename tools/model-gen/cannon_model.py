"""Genera el modelo 3D del cañón de papas (JSON de Minecraft) y sus texturas por etapa de oxidación.

Salida en la carpeta indicada:
  spud_cannon_base.json, spud_cannon_base_charged.json   modelos con la geometría (textura "#t")
  <etapa>.json, <etapa>_charged.json                     hijos que solo cambian la textura
  <etapa>_model.png                                      atlas 16x16 de cada etapa

Uso: python cannon_model.py <carpeta_salida>
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
IR = [H('#1e2026'), H('#3c4048'), H('#5e636e'), H('#8e939e'), H('#c8ccd4')]
WD = [H('#22120a'), H('#3e2214'), H('#5c3420'), H('#7a4a2c')]
K = H('#0e0806')
W = H('#ece8d8')
R = H('#c03030')
P = [H('#9a5500'), H('#c8973a'), H('#e9ba62'), H('#f8d086')]

# Rampas de 6 tonos (oscuro -> brillo) por etapa; derivadas de las texturas vanilla de cobre.
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

    # Cobre (filas 0-3): brillo con destellos, costura central y sombra con variación
    for x in range(16):
        tex[0][x] = BR[5] if x in (3, 4, 11) else BR[4]
        tex[1][x] = BR[3] if x % 8 else BR[4]
        tex[2][x] = BR[2] if x in (7, 8) else BR[3]
        tex[3][x] = BR[2] if x % 5 else BR[1]
    # Hierro (filas 4-7) con remaches
    for x in range(16):
        tex[4][x] = IR[3]
        tex[5][x] = IR[2]
        tex[6][x] = IR[2]
        tex[7][x] = IR[1]
    for x in (2, 7, 12):
        tex[5][x] = IR[4]
    # Madera (filas 8-11) con veta
    for x in range(16):
        tex[8][x] = WD[3]
        tex[9][x] = WD[2] if x % 4 else WD[1]
        tex[10][x] = WD[2] if (x + 2) % 5 else WD[1]
        tex[11][x] = WD[1]
    # Boca (0..4, 12..16): anillo de cobre con abertura oscura
    fill(0, 12, 4, 16, BR[2])
    fill(1, 13, 3, 15, K)
    tex[12][0] = BR[4]
    tex[12][1] = BR[4]
    tex[13][0] = BR[3]
    tex[15][3] = BR[1]
    tex[14][3] = BR[1]
    # Manómetro (4..8, 12..16): borde de hierro, esfera blanca, marca y aguja roja
    fill(4, 12, 8, 16, IR[2])
    for (x, y) in ((4, 12), (7, 12), (4, 15), (7, 15)):
        tex[y][x] = IR[1]
    fill(5, 13, 7, 15, W)
    tex[12][5] = IR[4]
    tex[12][6] = IR[3]
    tex[13][5] = R
    tex[14][6] = H('#3a3a3a')
    # Papa (8..12, 12..16)
    fill(8, 12, 12, 16, P[1])
    tex[12][8] = P[3]
    tex[12][9] = P[2]
    tex[13][8] = P[2]
    tex[15][11] = P[0]
    tex[14][11] = P[0]
    # Abertura oscura (12..16, 12..16)
    fill(12, 12, 16, 16, K)
    return tex


def png(path, img):
    raw = b"".join(b"\x00" + b"".join(bytes(c) for c in row) for row in img)

    def chunk(tag, data):
        return struct.pack(">I", len(data)) + tag + data + struct.pack(">I", zlib.crc32(tag + data) & 0xffffffff)

    with open(path, "wb") as f:
        f.write(b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", 16, 16, 8, 6, 0, 0, 0))
                + chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b""))


UV = {'copper': [0, 0, 16, 4], 'iron': [0, 4, 16, 8], 'wood': [0, 8, 16, 12], 'muzzle': [0, 12, 4, 16],
      'gauge': [4, 12, 8, 16], 'potato': [8, 12, 12, 16], 'bore': [12, 12, 16, 16]}


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


def cyl(x0, x1, r, side, east=None, west=None, name=""):
    """Cilindro octogonal (apotema r) a lo largo del eje X: dos rectángulos en cruz + cuadrado central.
    Los extremos se escalonan 0.04 px para que ninguna cara comparta plano (evita z-fighting)."""
    t = r * 0.414
    q = r * 0.707
    e = 0.04
    return [box([x0, 8 - r, 8 - t], [x1, 8 + r, 8 + t], side, east, west, name=name + "_v"),
            box([x0 - e, 8 - t, 8 - r], [x1 + e, 8 + t, 8 + r], side, east, west, name=name + "_h"),
            box([x0 - 2 * e, 8 - q, 8 - q], [x1 + 2 * e, 8 + q, 8 + q], side, east, west, name=name + "_c")]


def build(loaded=False):
    els = []
    els += cyl(1.0, 3.0, 2.5, 'copper', name="tapa")                      # tapa trasera redondeada
    els += cyl(2.5, 7.0, 3.1, 'copper', name="recamara")                  # recámara gruesa
    els += cyl(6.6, 7.6, 3.4, 'iron', name="aro1")                        # aro de hierro
    els += cyl(7.4, 14.0, 2.2, 'copper', name="tubo")                     # tubo
    els += cyl(11.0, 11.8, 2.6, 'iron', name="aro2")                      # aro de hierro
    els += cyl(13.4, 15.6, 3.3, 'copper', east='muzzle', name="boca")     # boca ancha
    els += cyl(15.0, 15.45, 3.5, 'iron', name="reborde")                  # reborde de hierro en la boca
    els.append(box([15.55, 6.9, 6.9], [15.65, 9.1, 9.1], 'bore', name="abertura"))
    for (rx, rr) in ((7.1, 3.4), (11.4, 2.6)):                           # remaches en los aros
        els.append(box([rx - 0.3, 8 + rr, 7.7], [rx + 0.3, 8 + rr + 0.35, 8.3], 'iron', name="remache_arriba"))
        els.append(box([rx - 0.3, 7.7, 8 + rr], [rx + 0.3, 8.3, 8 + rr + 0.35], 'iron', name="remache_lado"))
    # Manómetro: tubito + esfera redonda sobre la recámara
    els.append(box([4.4, 10.4, 7.6], [5.2, 11.4, 8.4], 'iron', name="tubo_manometro"))
    els.append(box([3.3, 11.2, 6.5], [6.3, 12.0, 9.5], 'iron', up='gauge', name="manometro_a"))
    els.append(box([3.8, 11.24, 6.0], [5.8, 11.96, 10.0], 'iron', up='gauge', name="manometro_b"))
    # Tolva de carga: embudo de cobre sobre el tubo, por donde entran las papas
    els.append(box([8.6, 9.9, 7.2], [10.2, 11.2, 8.8], 'copper', name="tolva_cuello"))
    els.append(box([8.1, 11.1, 6.7], [10.7, 12.1, 9.3], 'copper', up='bore', name="tolva_boca"))
    els.append(box([7.95, 12.05, 6.55], [10.85, 12.45, 9.45], 'iron', up='muzzle', name="tolva_reborde"))
    # Empuñadura de madera con pomo de hierro
    els.append(box([3.6, 2.0, 7.0], [5.6, 5.8, 9.0], 'wood', name="empunadura"))
    els.append(box([3.4, 1.5, 6.8], [5.8, 2.1, 9.2], 'iron', name="pomo"))
    if loaded:
        els.append(box([14.6, 7.0, 7.0], [16.4, 9.0, 9.0], 'potato', name="papa"))
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


def model(**kw):
    return {"textures": {"t": "misfitrelics:item/spud_cannon_model", "particle": "#t"},
            "gui_light": "side", "elements": build(**kw), "display": DISPLAY}


def dump(obj, name):
    with open(os.path.join(OUT, name), "w", encoding="utf-8") as f:
        json.dump(obj, f, indent=1)


sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import mclib  # noqa: E402  (texturizado por cara compartido)

_charged = build(loaded=True)
_base = [e for e in _charged if e["name"] != "papa"]
mclib.apply_vanilla(_charged, UV, {'copper': 'copper', 'muzzle': 'copper', 'iron': 'iron', 'wood': 'wood'})
_t0 = make_texture(STAGES['spud_cannon'])
_spec = {n: mclib.cut(_t0, UV[n]) for n in ('gauge', 'potato', 'bore')}
_paint = mclib.repaint(_charged, UV, {}, _spec, size=16)
mclib.png(os.path.join(OUT, "pressure_spud_cannon_details.png"), _paint({}, _spec))
COPPER_BY_STAGE = {'spud_cannon': 'minecraft:block/copper_block', 'exposed_spud_cannon': 'minecraft:block/exposed_copper', 'weathered_spud_cannon': 'minecraft:block/weathered_copper', 'oxidized_spud_cannon': 'minecraft:block/oxidized_copper'}
_tex = {"t": "misfitrelics:block/pressure_spud_cannon_details", "copper": "minecraft:block/copper_block",
        "iron": "minecraft:block/anvil", "wood": "minecraft:block/dark_oak_planks", "particle": "#copper"}
for _name, _els in (("spud_cannon_base.json", _base), ("spud_cannon_base_charged.json", _charged)):
    dump({"textures": _tex, "gui_light": "side", "elements": _els, "display": DISPLAY}, _name)
mclib.dump(COPPER_BY_STAGE, os.path.join(OUT, "pressure_stages.json"))
print("ok", len(_base), "elementos; solapadas:", len(mclib.coplanar_overlaps(_charged)))
