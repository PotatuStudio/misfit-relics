"""Utilidades compartidas para generar modelos de ítem de Minecraft (JSON + texturas 16x16)."""
import itertools
import json
import os
import struct
import zlib


def H(s):
    return tuple(int(s[i:i + 2], 16) for i in (1, 3, 5)) + (255,)


T = (0, 0, 0, 0)

# Rampas de cobre (oscuro -> brillo) por etapa, derivadas de las texturas vanilla.
COPPER_STAGES = {
    "": [H('#421c11'), H('#5f2c1c'), H('#9c4e31'), H('#c15a36'), H('#e3826c'), H('#fc9982')],
    "exposed_": [H('#3e3229'), H('#5e4c3f'), H('#796454'), H('#947661'), H('#ba8277'), H('#ce8d83')],
    "weathered_": [H('#26382f'), H('#3e5446'), H('#497164'), H('#6c975c'), H('#66a977'), H('#7ab799')],
    "oxidized_": [H('#1c3a30'), H('#2a5244'), H('#3b6655'), H('#3e816b'), H('#4fab90'), H('#6ec59f')],
}


def png(path, img):
    raw = b"".join(b"\x00" + b"".join(bytes(c) for c in row) for row in img)

    def chunk(tag, data):
        return struct.pack(">I", len(data)) + tag + data + struct.pack(">I", zlib.crc32(tag + data) & 0xffffffff)

    with open(path, "wb") as f:
        f.write(b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", len(img[0]), len(img), 8, 6, 0, 0, 0))
                + chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b""))


def blank():
    return [[T] * 16 for _ in range(16)]


class ModelBuilder:
    """Construye elementos con un mapa de UV por material."""

    def __init__(self, uv):
        self.uv = uv
        self.elements = []

    def faces(self, side, overrides=None):
        f = {k: {"uv": self.uv[side], "texture": "#t"} for k in ('north', 'south', 'up', 'down', 'east', 'west')}
        for k, v in (overrides or {}).items():
            f[k] = {"uv": self.uv[v], "texture": "#t"}
        return f

    def box(self, frm, to, side, name="", rotation=None, **overrides):
        e = {"name": name, "from": [round(v, 3) for v in frm], "to": [round(v, 3) for v in to],
             "faces": self.faces(side, overrides)}
        if rotation:
            e["rotation"] = rotation
        self.elements.append(e)
        return e

    def cyl(self, axis, a0, a1, r, side, center=(8.0, 8.0), name="", **overrides):
        """Cilindro octogonal a lo largo de axis ('x','y','z') con apotema r.
        Tres prismas (cruz + cuadrado central) con extremos escalonados 0.04 px contra z-fighting.
        overrides: caras de los extremos, p. ej. end='muzzle' (extremo positivo) o start='...'."""
        t, q, e = r * 0.414, r * 0.707, 0.04
        idx = {'x': 0, 'y': 1, 'z': 2}[axis]
        others = [i for i in range(3) if i != idx]
        end_face = {'x': ('east', 'west'), 'y': ('up', 'down'), 'z': ('south', 'north')}[axis]
        ov = {}
        if 'end' in overrides:
            ov[end_face[0]] = overrides['end']
        if 'start' in overrides:
            ov[end_face[1]] = overrides['start']
        for (h0, h1, ext) in ((r, t, 0), (t, r, e), (q, q, 2 * e)):
            frm, to = [0, 0, 0], [0, 0, 0]
            frm[idx], to[idx] = a0 - ext, a1 + ext
            frm[others[0]], to[others[0]] = center[0] - h0, center[0] + h0
            frm[others[1]], to[others[1]] = center[1] - h1, center[1] + h1
            self.box(frm, to, side, name=name, **ov)


def coplanar_overlaps(elements):
    """Pares de caras con la misma orientación, en el mismo plano y que se solapan (posible z-fighting)."""
    faces = []
    for el in elements:
        if el.get("rotation"):
            continue
        a, b = el["from"], el["to"]
        for ax in range(3):
            o = [k for k in range(3) if k != ax]
            for side, val in ((0, a[ax]), (1, b[ax])):
                faces.append((ax, side, round(val, 4), (a[o[0]], b[o[0]], a[o[1]], b[o[1]]), el["name"], id(el)))
    bad = []
    for f, g in itertools.combinations(faces, 2):
        if f[:3] == g[:3] and f[5] != g[5]:
            r1, r2 = f[3], g[3]
            if r1[0] < r2[1] and r2[0] < r1[1] and r1[2] < r2[3] and r2[2] < r1[3]:
                bad.append((f[4], g[4]))
    return bad


def dump(obj, path):
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(obj, f, indent=1)
        f.write("\n")


# Transformaciones de cámara para objetos alargados en el eje X (boca o campana hacia +X).
HELD_DISPLAY = {
    "gui": {"rotation": [25, -30, 35], "translation": [0, 0, 0], "scale": [0.85, 0.85, 0.85]},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.5, 0.5, 0.5]},
    "fixed": {"rotation": [0, 90, 0], "translation": [0, 0, 0], "scale": [0.9, 0.9, 0.9]},
    "thirdperson_righthand": {"rotation": [0, 90, 180], "translation": [0, 2, 1], "scale": [0.8, 0.8, 0.8]},
    "thirdperson_lefthand": {"rotation": [0, -90, 180], "translation": [0, 2, 1], "scale": [0.8, 0.8, 0.8]},
    "firstperson_righthand": {"rotation": [0, 90, -5], "translation": [1.5, 3, 0], "scale": [0.7, 0.7, 0.7]},
    "firstperson_lefthand": {"rotation": [0, -90, 5], "translation": [1.5, 3, 0], "scale": [0.7, 0.7, 0.7]},
    "head": {"rotation": [0, -90, 0], "translation": [0, 10, 0], "scale": [1, 1, 1]},
}


# ---------------------------------------------------------------------------
# Texturizado por cara (como el "box UV" de Blockbench): cada cara recibe su propio rectángulo en el atlas,
# del tamaño exacto de la cara (1 px por unidad, sin estirar), y se pinta con un sombreado coherente:
# luz desde arriba; caras superiores claras, inferiores oscuras; costados con borde superior de brillo y
# borde inferior de sombra. Nada al azar.
# ---------------------------------------------------------------------------
FACE_DIMS = {'north': (0, 1), 'south': (0, 1), 'east': (2, 1), 'west': (2, 1), 'up': (0, 2), 'down': (0, 2)}


def shade_face(ramp, w, h, kind):
    """Pinta una cara de w x h solo con iluminación direccional (luz desde arriba):
    caras de arriba claras, costados en tono medio, caras de abajo oscuras. Sin bordes dibujados ni ruido."""
    color = {'up': ramp[-2], 'down': ramp[-4]}.get(kind, ramp[-3])
    return [[color] * w for _ in range(h)]


def repaint(elements, uv_table, painters, specials, size=64):
    """Reasigna las UV de cada cara a un rectángulo propio del atlas y devuelve una función que pinta el atlas.

    uv_table: material -> uv original (sirve para saber el material de cada cara)
    painters: material -> rampa (lista de colores) o función (w, h, kind) -> imagen
    specials: material -> imagen fija (p. ej. la boca, el manómetro); se comparte entre caras
    Devuelve paint(painters, specials) -> atlas size x size.
    """
    inv = {tuple(v): k for k, v in uv_table.items()}
    slots = {}            # clave -> (x, y, w, h)
    pending = []          # (clave, w, h)
    face_keys = []
    for el in elements:
        a, b = el['from'], el['to']
        for fname, face in el['faces'].items():
            if face.get('texture', '#t') != '#t':
                continue
            mat = inv.get(tuple(face['uv']))
            if mat is None:
                continue
            if mat in specials:
                img = specials[mat]
                key = ('special', mat, len(img[0]), len(img))
            else:
                i, j = FACE_DIMS[fname]
                w = max(1, round(abs(b[i] - a[i])))
                h = max(1, round(abs(b[j] - a[j])))
                kind = fname if fname in ('up', 'down') else 'side'
                key = (mat, kind, w, h)
            if key not in slots and key not in [p[0] for p in pending]:
                pending.append((key, key[2], key[3]))
            face_keys.append((face, key))
    # Empaquetado por estantes, de mayor a menor alto
    x = y = row_h = 0
    for key, w, h in sorted(pending, key=lambda p: -p[2]):
        if x + w > size:
            x, y, row_h = 0, y + row_h + 1, 0
        if y + h > size:
            raise ValueError("el atlas de %d px no alcanza" % size)
        slots[key] = (x, y, w, h)
        x += w + 1
        row_h = max(row_h, h)
    k = 16.0 / size
    for face, key in face_keys:
        sx, sy, w, h = slots[key]
        face['uv'] = [round(sx * k, 4), round(sy * k, 4), round((sx + w) * k, 4), round((sy + h) * k, 4)]

    def paint(painters_, specials_):
        tex = [[T] * size for _ in range(size)]
        for key, (sx, sy, w, h) in slots.items():
            if key[0] == 'special':
                img = specials_[key[1]]
            else:
                mat, kind = key[0], key[1]
                p = painters_[mat]
                img = p(w, h, kind) if callable(p) else shade_face(p, w, h, kind)
            for yy in range(h):
                for xx in range(w):
                    tex[sy + yy][sx + xx] = img[yy][xx]
        return tex
    return paint


def cut(tex, uv):
    """Recorta de una textura 16x16 el bloque de una región UV (en píxeles enteros)."""
    u0, v0, u1, v1 = [int(round(c)) for c in uv]
    return [row[u0:u1] for row in tex[v0:v1]]


# ---------------------------------------------------------------------------
# Texturas vanilla referenciadas: cada cara toma un pedazo de una textura del juego (p. ej.
# minecraft:block/copper_block) del tamaño exacto de la cara, 1 px por unidad, como los modelos vanilla.
# No se copia ningún archivo: el modelo solo referencia la textura del juego.
# ---------------------------------------------------------------------------
def apply_vanilla(elements, uv_table, mapping, rotations=None):
    """mapping: material -> variable de textura (p. ej. 'copper' -> 'copper').
    rotations: material -> {cara: grados} para orientar texturas como la del pistón."""
    inv = {tuple(v): k for k, v in uv_table.items()}
    for idx, el in enumerate(elements):
        a, b = el['from'], el['to']
        for fname, face in el['faces'].items():
            mat = inv.get(tuple(face['uv']))
            if mat not in mapping:
                continue
            i, j = FACE_DIMS[fname]
            w = min(16, max(1, round(abs(b[i] - a[i]))))
            h = min(16, max(1, round(abs(b[j] - a[j]))))
            # desplazamiento determinista para que piezas vecinas no repitan el mismo pedazo
            u0 = (idx * 5 + len(fname) * 3) % (16 - w + 1)
            v0 = (idx * 3 + len(fname)) % (16 - h + 1)
            face['uv'] = [u0, v0, u0 + w, v0 + h]
            face['texture'] = '#' + mapping[mat]
            rot = (rotations or {}).get(mat, {}).get(fname)
            if rot:
                face['rotation'] = rot
