# Generador de modelos 3D

Scripts que generan los modelos 3D de los ítems (JSON de Minecraft), el visor interactivo para revisarlos
antes de probar en el juego, y el exportador a `src/main/resources`.

Los modelos referencian **texturas vanilla** (`minecraft:block/copper_block`, `piston_side`, `anvil`, …):
no se copia ningún asset de Mojang. Solo las piezas sin equivalente en el juego (boca, manómetro, papa,
gema, cuero, silueta del murciélago) van en un atlas pequeño de detalles por ítem.

## Uso
```sh
mkdir build
python cannon_model.py build     # cañón a presión (steampunk)
python rustic_model.py build     # cañón rústico (crafteable)
python trumpet_model.py build    # trompeta de cobre
python collar_model.py build     # collar del murciélago

# Visor (abre el HTML en el navegador). Necesita el jar del cliente 26.3 solo para la vista previa:
python make_viewer.py build build/visor.html <ruta>/client.jar

# Exportar al mod
python export_to_mod.py build ../../src/main/resources
```

Cada script imprime cuántas caras quedan **solapadas en el mismo plano** (z-fighting): debe ser 0.

## Reglas que siguen
- 1 píxel de textura por unidad del modelo, sin estirar (cada cara toma un pedazo de su tamaño exacto).
- Tubos redondos como octágonos de 3 piezas con extremos escalonados 0.04 px (evita parpadeos).
- Las etapas de oxidación son modelos hijos que solo cambian las texturas de cobre.
