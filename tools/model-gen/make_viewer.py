"""Genera el visor HTML interactivo (three.js) de los modelos del mod.

Las texturas vanilla (minecraft:block/...) se leen del jar local de Minecraft solo para la vista previa;
el mod no las incluye, solo las referencia.

Uso: python make_viewer.py <carpeta_modelo> <salida.html> <ruta al jar de Minecraft 26.3>
"""
import base64
import json
import os
import sys
import zipfile

model_dir, out = sys.argv[1], sys.argv[2]
JAR = sys.argv[3]
jar = zipfile.ZipFile(JAR)
STAGE_LABELS = {"": "Cobre", "exposed_": "Expuesto", "weathered_": "Desgastado", "oxidized_": "Oxidado"}
CANNON_STAGE = {"spud_cannon": "", "exposed_spud_cannon": "exposed_", "weathered_spud_cannon": "weathered_",
                "oxidized_spud_cannon": "oxidized_"}
_cache = {}


def j(name):
    with open(os.path.join(model_dir, name + ".json"), encoding="utf-8") as f:
        return json.load(f)


def tex_b64(ref):
    """Resuelve una referencia de textura a PNG en base64."""
    if ref not in _cache:
        ns, path = ref.split(":", 1)
        if ns == "minecraft":
            data = jar.read(f"assets/minecraft/textures/{path}.png")
        else:
            with open(os.path.join(model_dir, path.split("/")[-1] + ".png"), "rb") as f:
                data = f.read()
        _cache[ref] = base64.b64encode(data).decode()
    return _cache[ref]


def stage_maps(base_textures, overrides):
    """overrides: clave de etapa -> {variable: referencia}; devuelve etapa -> {variable: b64}."""
    out_maps = {}
    for stage, ov in overrides.items():
        merged = {k: v for k, v in base_textures.items() if not v.startswith("#")}
        merged.update(ov)
        out_maps[stage] = {k: tex_b64(v) for k, v in merged.items()}
    return out_maps


def cannon(base_name, charged_name, stages_file, label):
    base = j(base_name)
    raw = j(stages_file)
    overrides = {CANNON_STAGE[k]: {"copper": v} for k, v in raw.items()}
    return {"label": label, "base": base, "charged": j(charged_name), "textures": stage_maps(base["textures"], overrides)}


trumpet = j("trumpet_base")
collar = j("collar_base")
ITEMS = {
    "canon": {"label": "Cañón de papas", "variants": {
        "rustico": cannon("rustic_base", "rustic_base_charged", "rustic_stages", "Rústico (crafteable)"),
        "presion": cannon("spud_cannon_base", "spud_cannon_base_charged", "pressure_stages", "A presión (botín)"),
    }},
    "trompeta": {"label": "Trompeta de cobre", "variants": {
        "trompeta": {"label": "Trompeta", "base": trumpet,
                     "textures": stage_maps(trumpet["textures"], j("trumpet_stages"))},
    }},
    "collar": {"label": "Collar del murciélago", "variants": {
        "collar": {"label": "Collar", "base": collar, "worn": j("collar_worn"),
                   "textures": stage_maps(collar["textures"], {"": {}})},
    }},
}
with open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "viewer_template.html"), encoding="utf-8") as f:
    template = f.read()
html = template.replace("__DATA__", json.dumps(ITEMS)).replace("__STAGES__", json.dumps(STAGE_LABELS))
with open(out, "w", encoding="utf-8") as f:
    f.write(html)
print(out)
