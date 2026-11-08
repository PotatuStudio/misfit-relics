"""Modelo 3D de la trompeta de cobre: boquilla, tubo de entrada, tres pistones, vuelta de afinación y campana.

Salida: trumpet_base.json y trumpet_<etapa>_model.png (4 etapas de oxidación).
Uso: python trumpet_model.py <carpeta_salida>
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mclib import H, T, COPPER_STAGES, HELD_DISPLAY, ModelBuilder, blank, coplanar_overlaps, cut, dump, png, repaint  # noqa: E402

OUT = sys.argv[1]
PEARL = [H('#8e8878'), H('#c8c2b0'), H('#ece6d6')]       # botones de los pistones (nácar)
DARK = H('#140a06')


def make_texture(C):
    tex = blank()
    # Cobre liso (filas 0-3) y cobre oscuro liso para el bloque de pistones (filas 4-7):
    # un solo color por material; el volumen lo da la iluminación del juego.
    for y in range(0, 4):
        for x in range(16):
            tex[y][x] = C[3]
    for y in range(4, 8):
        for x in range(16):
            tex[y][x] = C[2]
    # Nácar de los botones (0..4, 8..12)
    for y in range(8, 12):
        for x in range(0, 4):
            tex[y][x] = PEARL[2] if y == 8 else PEARL[1] if y < 11 else PEARL[0]
    # Interior de la campana (4..8, 8..12): anillo de cobre y fondo oscuro
    for y in range(8, 12):
        for x in range(4, 8):
            tex[y][x] = C[2]
    for y in range(9, 11):
        for x in range(5, 7):
            tex[y][x] = DARK
    # Boquilla (8..12, 8..12): copa con agujero
    for y in range(8, 12):
        for x in range(8, 12):
            tex[y][x] = C[4]
    tex[9][9] = DARK
    tex[10][10] = DARK
    tex[9][10] = C[1]
    tex[10][9] = C[1]
    return tex


UV = {'copper': [0, 0, 16, 4], 'dark': [0, 4, 16, 8], 'pearl': [0, 8, 4, 12], 'bell': [4, 8, 8, 12],
      'mouth': [8, 8, 12, 12], 'hole': [12, 8, 16, 12]}


def build():
    """Versión simple y cuadrada: boquilla, tubo, bloque ancho de pistones con 3 botones, vuelta y campana."""
    m = ModelBuilder(UV)

    def tube(axis, a0, a1, half, cy, cz=8.0, cx=None, name="", **ov):
        """Tubo de sección cuadrada (una sola pieza) a lo largo de X o Y."""
        if axis == 'x':
            m.box([a0, cy - half, cz - half], [a1, cy + half, cz + half], 'copper', name=name, **ov)
        else:
            m.box([cx - half, a0, cz - half], [cx + half, a1, cz + half], 'copper', name=name, **ov)

    Y_LEAD, Y_BELL, Y_LOOP = 9.0, 7.6, 5.6
    tube('x', 0.8, 1.8, 0.75, Y_LEAD, name="boquilla", west='mouth')
    tube('x', 1.8, 7.0, 0.5, Y_LEAD, name="tubo_entrada")
    m.box([6.8, 5.2, 7.0], [11.6, 10.2, 9.0], 'dark', name="pistones")      # bloque ancho al medio
    for i, x in enumerate((7.6, 9.2, 10.8)):
        m.box([x - 0.5, 10.2, 7.5], [x + 0.5, 10.9, 8.5], 'pearl', name=f"boton{i}")
    tube('x', 3.5, 7.0, 0.45, Y_LOOP, name="vuelta_inferior")
    tube('y', Y_LOOP - 0.4, Y_LEAD - 0.4, 0.42, None, cx=3.5, name="vuelta_codo")
    tube('x', 11.6, 12.9, 0.55, Y_BELL, name="tubo_campana")
    tube('x', 12.9, 14.1, 1.1, Y_BELL, name="campana0")
    tube('x', 14.1, 15.6, 2.0, Y_BELL, name="campana1")
    m.box([15.58, Y_BELL - 1.3, 6.7], [15.68, Y_BELL + 1.3, 9.3], 'hole', name="interior_campana")
    return m.elements


def model():
    return {"textures": {"t": "misfitrelics:item/copper_trumpet_model", "particle": "#t"},
            "gui_light": "side", "elements": build(), "display": HELD_DISPLAY}


if __name__ == "__main__":
    els = build()
    mclib_vanilla = {'copper': 'copper', 'mouth': 'copper', 'bell': 'copper', 'dark': 'cut_copper', 'pearl': 'pearl'}
    from mclib import apply_vanilla
    apply_vanilla(els, UV, mclib_vanilla)
    spec = {'hole': [[DARK] * 4 for _ in range(4)]}
    paint = repaint(els, UV, {}, spec, size=16)
    png(os.path.join(OUT, "copper_trumpet_details.png"), paint({}, spec))
    tex = {"t": "misfitrelics:block/copper_trumpet_details", "copper": "minecraft:block/copper_block",
           "cut_copper": "minecraft:block/cut_copper", "pearl": "minecraft:block/quartz_block_top", "particle": "#copper"}
    dump({"textures": tex, "gui_light": "side", "elements": els, "display": HELD_DISPLAY},
         os.path.join(OUT, "trumpet_base.json"))
    stages = {"": {}, "exposed_": {}, "weathered_": {}, "oxidized_": {}}
    for prefix in stages:
        stages[prefix] = {"copper": f"minecraft:block/{prefix}copper" if prefix else "minecraft:block/copper_block",
                          "cut_copper": f"minecraft:block/{prefix}cut_copper"}
    dump(stages, os.path.join(OUT, "trumpet_stages.json"))
    print("trompeta:", len(els), "piezas; caras solapadas:", len(coplanar_overlaps(els)))
