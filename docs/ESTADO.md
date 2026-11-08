# Estado del proyecto (para retomar)

Última sesión: 2026-11-08. Todo lo hecho está en `main` (último commit de código: "Arte de reliquias más vanilla, rayos
animados en los íconos e ícono del mod nuevo"). Las reliquias, sus números y dónde encontrarlas están en el
[README](../README.md); la historia, en el [CHANGELOG](../CHANGELOG.md).

## Cómo se trabaja
- Se desarrolla en el PC de desarrollo y **se prueba en el otro PC** (`git pull` + `./gradlew runClient`) cuando está en `main`.
  No lanzar el cliente en el PC de desarrollo salvo que se pida.
- Verificar la compilación con el build de IntelliJ (MCP) o, si se cuelga, con javac (los únicos errores esperables son los
  métodos que inyecta Loom: attachments y `CreativeModeTab.Output`).
- Arte: herramientas en `tools/model-gen` (íconos 2D: `ring_icons.py`, `relic_art.py`, `combat_icons.py`,
  `mobility_icons.py`, `collar_icon.py`, `mod_icon.py`; modelos 3D de cañones y trompeta; `export_to_mod.py` lo copia todo
  al mod; `gui_icons.py` genera los íconos 2D de cañones y trompeta a partir del render del visor).

## Pendiente
1. **Portar a Fabric + NeoForge con Architectury** (decisión del 2026-11-08), con el mismo esquema que Mythic Relics
   (ver `docs/ESTADO.md` de ese repo para la tabla Fabric API → Architectury API). Trinkets Updated ya funciona en ambos.
2. **Libro de reliquias en el juego** con Modonomicon y **wiki en `docs/wiki/`**, generados desde los mismos datos que
   el botín (`ModLoot.sources`) y los tooltips.
3. Ideas aprobadas sin implementar (elegir con el usuario): anillo de escarcha, anillo lunar, pulsera del evocador,
   puños de golem, sombrero de la bruja, corona del illager, auriculares de sculk, bufanda de phantom, capa de ender,
   mochila del saqueador, cinturón de pólvora, botas de pisotón, amuleto del ajolote, ojo del guardián, campana del
   shrieker, diente de gigante. Sistemas: rarezas visibles, bonos de conjunto, reliquias malditas, mejorar reliquias en
   la mesa de herrería, altar de reliquias.
4. Revisar en el juego (el usuario no lo ha reportado aún): posición de los carcajes en la espalda y del corazón y la
   concha en el pecho; si el tinte de las botas de magma y de la concha se ve bien.
5. Primera release `v0.1.0` en Modrinth cuando el usuario lo pida.
