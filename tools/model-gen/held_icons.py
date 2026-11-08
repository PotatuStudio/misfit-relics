"""Íconos 2D (16x16) del cañón rústico, el cañón a presión y la trompeta, para el inventario (en la mano siguen los
modelos 3D). Estilo vanilla, como el catalejo: piezas en diagonal exacta de 45°, con franjas de sombreado a lo ancho
(luz arriba a la izquierda) y contorno con el tono más oscuro de cada material. Cada etapa de oxidación cambia la
rampa de cobre (mclib.COPPER_STAGES, sacadas de las texturas vanilla).

Coordenadas en diagonal: a = x - y (a lo largo, crece hacia arriba a la derecha) y c = x + y (a lo ancho, crece
hacia abajo a la derecha).

Salida: <id>_icon.png por etapa y la hoja de vista previa iconos_2d.png
Uso: python held_icons.py <carpeta_salida>
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mclib import COPPER_STAGES, H, T, png  # noqa: E402

PREFIXES = ["", "exposed_", "weathered_", "oxidized_"]
WOOD = [H('#3b2814'), H('#5c4024'), H('#7d5b36'), H('#a07c4f'), H('#c2a170'), H('#d8bb8a')]
IRON = [H('#26282d'), H('#43464e'), H('#626670'), H('#868b95'), H('#aeb3bc'), H('#d4d8de')]
STRING = [H('#5a5a5a'), H('#8c8c8c'), H('#b4b4b4'), H('#d2d2d2'), H('#ececec'), H('#ffffff')]
QUARTZ = [H('#5f5a52'), H('#9a9386'), H('#c9c2b4'), H('#e3ddd0'), H('#f3eee4'), H('#ffffff')]
HOLE = H('#160f0b')
RED = H('#c23a2a')


class Icon:
    def __init__(self):
        self.px = [[None] * 16 for _ in range(16)]

    def put(self, x, y, color):
        if 0 <= x < 16 and 0 <= y < 16:
            self.px[y][x] = color

    def tube(self, c0, a0, a1, shades, cap0=None, cap1=None):
        """Tubo diagonal: a lo ancho c0..c0+len(shades)-1 con esos colores; a lo largo a0..a1.
        cap0/cap1: color del contorno en cada punta (None = sin tapa)."""
        for y in range(16):
            for x in range(16):
                a, c = x - y, x + y
                k = c - c0
                if a0 <= a <= a1 and 0 <= k < len(shades):
                    col = shades[k]
                    if (a == a0 and cap0 is not None) or (a == a1 and cap1 is not None):
                        col = cap0 if a == a0 else cap1
                    self.px[y][x] = col

    def cells(self, c0, width, a):
        return [(x, y) for y in range(16) for x in range(16) if x - y == a and 0 <= x + y - c0 < width]

    def image(self):
        return [[c if c is not None else T for c in row] for row in self.px]


def shades(ramp, width):
    """Franjas a lo ancho: contorno, brillo, medios y sombra, contorno."""
    inner = {1: [4], 2: [4, 2], 3: [4, 3, 2], 4: [5, 4, 3, 2], 5: [5, 4, 3, 2, 1], 6: [5, 4, 4, 3, 2, 1], 7: [5, 5, 4, 3, 3, 2, 1]}[width - 2]
    return [ramp[0]] + [ramp[i] for i in inner] + [ramp[0]]


def band(i, c0, width, a, ramp):
    """Anillo alrededor del tubo: dos diagonales seguidas (una sola dejaría huecos a 45°), con su propio sombreado."""
    sh = shades(ramp, width)
    for aa in (a, a + 1):
        for y in range(16):
            for x in range(16):
                k = x + y - c0
                if x - y == aa and 0 <= k < width:
                    i.put(x, y, sh[k])


def muzzle(i, c0, width, a_end, rim):
    """Boca: borde claro en las dos últimas diagonales y el agujero oscuro adentro."""
    for aa in (a_end - 1, a_end):
        for y in range(16):
            for x in range(16):
                k = x + y - c0
                if x - y == aa and 0 <= k < width:
                    inside = 1 < k < width - 2
                    i.put(x, y, HOLE if inside else (rim[5] if 0 < k < width - 1 else rim[0]))


def rustic(cu):
    """Cañón rústico: caño de cobre atado con dos vueltas de cuerda, culata de pistón de madera y empuñadura."""
    i = Icon()
    W, c0 = 6, 12
    i.tube(c0, -8, 9, shades(cu, W), cap0=cu[0], cap1=cu[0])
    i.tube(c0 - 1, -10, -7, shades(WOOD, W + 2), cap0=WOOD[0], cap1=WOOD[0])   # culata de pistón
    for a in (-3, 3):
        band(i, c0, W, a, STRING)                                              # amarras de cuerda
    muzzle(i, c0, W, 9, cu)
    # Empuñadura de madera, inclinada hacia abajo desde la culata
    for x, y, col in ((5, 12, WOOD[3]), (4, 13, WOOD[3]), (5, 13, WOOD[2]), (3, 14, WOOD[3]), (4, 14, WOOD[2]),
                      (3, 15, WOOD[2]), (6, 12, WOOD[0]), (6, 13, WOOD[0]), (5, 14, WOOD[0]), (4, 15, WOOD[0]),
                      (2, 15, WOOD[0]), (2, 14, WOOD[0]), (3, 13, WOOD[0])):
        if i.px[y][x] is None or col != WOOD[0]:
            i.put(x, y, col)
    return i.image()


def pressure(cu):
    """Cañón a presión: caño de cobre largo con tres zunchos de hierro, manómetro de cuarzo y empuñadura de hierro."""
    i = Icon()
    W, c0 = 6, 13
    i.tube(c0, -11, 10, shades(cu, W), cap0=cu[0], cap1=cu[0])
    for a in (-11, -3, 5):
        band(i, c0 - 1, W + 2, a, IRON)                                        # zunchos
    muzzle(i, c0, W, 10, IRON)
    # Manómetro: esfera de cuarzo con aguja roja, asomando sobre el caño
    for x, y, col in ((6, 3, IRON[0]), (7, 3, IRON[0]), (5, 4, IRON[0]), (8, 4, IRON[0]), (5, 5, IRON[0]),
                      (6, 4, QUARTZ[5]), (7, 4, QUARTZ[3]), (6, 5, QUARTZ[3]), (7, 5, RED)):
        i.put(x, y, col)
    # Empuñadura de hierro
    for x, y, col in ((4, 13, IRON[4]), (3, 14, IRON[3]), (4, 14, IRON[2]), (3, 15, IRON[2]),
                      (5, 13, IRON[0]), (5, 14, IRON[0]), (4, 15, IRON[0]), (2, 15, IRON[0]), (2, 14, IRON[0])):
        if i.px[y][x] is None or col != IRON[0]:
            i.put(x, y, col)
    return i.image()


def trumpet(cu, cut):
    """Trompeta: boquilla abajo a la izquierda, tubo con tres pistones y campana ancha arriba a la derecha."""
    i = Icon()
    i.tube(15, -12, 1, shades(cu, 4), cap0=cu[0])                  # tubo
    i.tube(14, 1, 4, shades(cu, 6))                                # empieza a abrirse
    i.tube(12, 4, 8, shades(cu, 9), cap1=cu[0])                    # campana
    muzzle(i, 12, 9, 8, cu)
    # Tres pistones de cobre cortado que salen del tubo hacia arriba a la izquierda
    for k, (x, y) in enumerate(((4, 10), (6, 8), (8, 6))):
        i.put(x, y, cut[4] if k == 0 else cut[3])                  # vástago, pegado al borde del tubo
        i.put(x - 1, y - 1, cut[5])                                # botón
        i.put(x - 2, y - 2, cut[0])
    # Boquilla
    i.put(2, 13, cut[4])
    i.put(1, 14, cut[0])
    return i.image()


def sheet(images, path, scale=8):
    w = len(images) * (16 * scale + 8)
    canvas = [[H('#3a3a3a')] * w for _ in range(16 * scale)]
    for k, img in enumerate(images):
        for y in range(16 * scale):
            for x in range(16 * scale):
                p = img[y // scale][x // scale]
                if p[3]:
                    canvas[y][k * (16 * scale + 8) + x] = p
    png(path, canvas)


if __name__ == "__main__":
    out = sys.argv[1]
    previews = []
    for p in PREFIXES:
        cu = COPPER_STAGES[p]
        for name, img in ((p + "spud_cannon", rustic(cu)), (p + "pressure_spud_cannon", pressure(cu)),
                          (p + "copper_trumpet", trumpet(cu, cu))):
            png(os.path.join(out, name + "_icon.png"), img)
            if p in ("", "oxidized_"):
                previews.append(img)
    sheet(previews, os.path.join(out, "iconos_2d.png"))
    print("ok")
