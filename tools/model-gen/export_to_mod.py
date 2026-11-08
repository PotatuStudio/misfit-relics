"""Exporta los modelos aprobados (cañones, trompeta y collar) a los recursos del mod.

Los modelos referencian texturas vanilla (minecraft:block/...) y un atlas pequeño de detalles propios;
cada etapa de oxidación es un modelo hijo que solo cambia las texturas de cobre.

Uso: python export_to_mod.py <carpeta_scratch_modelo> <carpeta_resources_del_mod>
"""
import json
import os
import shutil
import sys

src, res = sys.argv[1], sys.argv[2]
A = os.path.join(res, "assets", "misfitrelics")
MODELS, TEXTURES, ITEMS = (os.path.join(A, d) for d in (os.path.join("models", "item"), os.path.join("textures", "item"), "items"))
# Las texturas de detalles van en textures/block: en 26.3 un modelo de ítem no puede mezclar atlas,
# y las texturas vanilla que usamos (cobre, pistón, yunque) son de bloque.
BLOCK_TEXTURES = os.path.join(A, "textures", "block")
for d in (MODELS, TEXTURES, ITEMS, BLOCK_TEXTURES):
    os.makedirs(d, exist_ok=True)

PREFIXES = ["", "exposed_", "weathered_", "oxidized_"]
COPPER = {p: (f"minecraft:block/{p}copper" if p else "minecraft:block/copper_block") for p in PREFIXES}
CUT_COPPER = {p: f"minecraft:block/{p}cut_copper" for p in PREFIXES}


def load(name):
    with open(os.path.join(src, name + ".json"), encoding="utf-8") as f:
        return json.load(f)


def dump(obj, path):
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(obj, f, indent=1)
        f.write("\n")


def model_ref(name):
    return {"type": "minecraft:model", "model": f"misfitrelics:item/{name}"}


def cannon(base_id, scratch_base, details, charge_ticks):
    for suffix in ("", "_charged"):
        model = load(scratch_base + suffix)
        if suffix:
            # Cargado: en primera persona el cañón sube y apunta un poco hacia arriba, para que se note que está listo.
            d = model["display"]
            d["firstperson_righthand"] = {**d["firstperson_righthand"], "rotation": [0, 90, 6], "translation": [1.5, 4.2, -0.5]}
            d["firstperson_lefthand"] = {**d["firstperson_lefthand"], "rotation": [0, -90, -6], "translation": [1.5, 4.2, -0.5]}
        dump(model, os.path.join(MODELS, f"{base_id}_base{suffix}.json"))
    shutil.copyfile(os.path.join(src, details + ".png"), os.path.join(BLOCK_TEXTURES, details + ".png"))
    for p in PREFIXES:
        item_id = p + base_id
        for suffix in ("", "_charged"):
            dump({"parent": f"misfitrelics:item/{base_id}_base{suffix}", "textures": {"copper": COPPER[p]}},
                 os.path.join(MODELS, f"{item_id}{suffix}.json"))
        # Mismo modelo 3D en el inventario y en la mano; al terminar de cargar asoma la papa.
        dump({"model": {
            "type": "minecraft:condition", "property": "minecraft:using_item",
            "on_false": model_ref(item_id),
            "on_true": {"type": "minecraft:range_dispatch", "property": "minecraft:use_duration",
                        "scale": round(1.0 / charge_ticks, 4),
                        "entries": [{"threshold": 1.0, "model": model_ref(item_id + "_charged")}],
                        "fallback": model_ref(item_id)}}},
            os.path.join(ITEMS, f"{item_id}.json"))


cannon("spud_cannon", "rustic_base", "spud_cannon_details", 10)
cannon("pressure_spud_cannon", "spud_cannon_base", "pressure_spud_cannon_details", 8)

# Trompeta: base + 4 etapas (cobre y cobre cortado cambian con la oxidación)
dump(load("trumpet_base"), os.path.join(MODELS, "copper_trumpet_base.json"))
shutil.copyfile(os.path.join(src, "copper_trumpet_details.png"), os.path.join(BLOCK_TEXTURES, "copper_trumpet_details.png"))
for p in PREFIXES:
    item_id = p + "copper_trumpet"
    dump({"parent": "misfitrelics:item/copper_trumpet_base",
          "textures": {"copper": COPPER[p], "cut_copper": CUT_COPPER[p]}}, os.path.join(MODELS, item_id + ".json"))
    dump({"model": model_ref(item_id)}, os.path.join(ITEMS, item_id + ".json"))

# Collar: ícono 2D (como los ítems vanilla) y la versión puesta que dibuja Trinkets en el pecho
# (cordones en diagonal y el murciélago, ver assets/misfitrelics/trinkets/bat_collar.json)
shutil.copyfile(os.path.join(src, "bat_collar_icon.png"), os.path.join(TEXTURES, "bat_collar.png"))
shutil.copyfile(os.path.join(src, "bat_collar_icon.png.mcmeta"), os.path.join(TEXTURES, "bat_collar.png.mcmeta"))
dump({"parent": "minecraft:item/generated", "textures": {"layer0": "misfitrelics:item/bat_collar"}},
     os.path.join(MODELS, "bat_collar.json"))
dump(load("collar_worn_pendant"), os.path.join(MODELS, "bat_collar_worn_pendant.json"))
dump(load("king_collar_worn_pendant"), os.path.join(MODELS, "king_bat_collar_worn_pendant.json"))
for _ext in (".png", ".png.mcmeta"):
    shutil.copyfile(os.path.join(src, "king_bat_collar_icon" + _ext), os.path.join(TEXTURES, "king_bat_collar" + _ext))
dump({"parent": "minecraft:item/generated", "textures": {"layer0": "misfitrelics:item/king_bat_collar"}},
     os.path.join(MODELS, "king_bat_collar.json"))
dump({"model": model_ref("king_bat_collar")}, os.path.join(ITEMS, "king_bat_collar.json"))
shutil.copyfile(os.path.join(src, "bat_collar_details.png"), os.path.join(BLOCK_TEXTURES, "bat_collar_details.png"))
dump({"model": model_ref("bat_collar")}, os.path.join(ITEMS, "bat_collar.json"))
print("exportado: 8 cañones, 4 trompetas y el collar")
