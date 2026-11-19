# Estado del proyecto (para retomar)

Última sesión: 2026-11-19. Las reliquias, sus números y dónde encontrarlas están en el [README](../README.md); la
historia, en el [CHANGELOG](../CHANGELOG.md).

## Sesión del 2026-11-19: port a Architectury
Misfit Relics quedó con la misma arquitectura que Mythic Relics (rama `feat/multiloader`):
- Módulos `common/` (todo el juego), `fabric/` y `neoforge/`, con los mismos `build.gradle`, versiones y herramientas
  de desarrollo (Mod Menu, Jade, JEI, Sodium, Iris, shaders Complementary). Sin Modonomicon por ahora (punto 2).
- Ayudantes comunes en `relic/`, iguales a los de Mythic donde aplica:
  - `WornRelics` (accesorio puesto, con caché por tick; `find` devuelve la pila viva para el collar);
  - `AccessoryHooks` (ponerse, quitarse y atributos; Fabric los conecta con `TrinketCallback`, NeoForge con `ICurioItem`);
  - `EntityData` (datos guardados con la entidad: attachments de Fabric API o de NeoForge, con los mismos ids que antes,
    así que los mundos de Fabric anteriores conservan sus datos);
  - `ServerState` (limpieza de mapas estáticos al desconectarse o cerrar el servidor; todavía no se usa en los mapas
    viejos: ver la auditoría).
- Registros con `DeferredRegister` de Architectury (`RegistrySupplier`: `.get()` para ítems y `.asHolder()` para
  efectos y pociones). Las gamerules se registran como las de vanilla (`Registries.GAME_RULE`).
- La pata de la suerte usaba `LootTableEvents.MODIFY_DROPS` de Fabric API, que Architectury no tiene: lo reemplaza
  `mixin/LootTableMixin` (misma inyección que Fabric API).
- Curios: `neoforge/.../TrinketsJsonCurioRenderer` dibuja las reliquias puestas leyendo los mismos
  `assets/misfitrelics/trinkets/*.json` de Trinkets (una sola fuente). Los collares usan su modelo puesto en el
  contexto `none` de la definición del ítem.
- Prueba automática: `./gradlew :fabric:runClientGameTest` (gamerules, datos de entidad, tooltips, cada accesorio puesto
  con capturas; falla si algo falla). Pasó entera el 2026-11-19. Al cerrar el juego sale un crash «Watchdog (post-main)»
  por hilos de un mod de desarrollo; también pasa en Mythic y no es del mod.
- NeoForge: `./gradlew :neoforge:runServer` arranca hasta «Done» con el mod cargado (servidor dedicado sin errores).
  **Sin probar en el juego**: el cliente de NeoForge, en especial cómo se ven las reliquias puestas con Curios.

## Auditoría
[AUDITORIA.md](AUDITORIA.md): corregida el mismo día (el usuario aprobó todo, también lo que cambia el juego). Quedan
pendientes: partir las clases grandes, números mágicos de `AlliedBat` y `PlagueRings`, y pruebas de juego con azar o
varios mundos. Pruebas unitarias: `./gradlew :common:test`.

## Cómo se trabaja
- Se desarrolla en el PC de desarrollo y **se prueba en el otro PC** (`git pull` + `./gradlew :fabric:runClient` o
  `:neoforge:runClient`). En el PC de desarrollo, la prueba automática de cliente sí se puede correr.
- Compilar con `./gradlew build` y `JAVA_HOME` apuntando al JDK 25.
- Arte: herramientas en `tools/model-gen` (íconos 2D: `ring_icons.py`, `relic_art.py`, `combat_icons.py`,
  `mobility_icons.py`, `collar_icon.py`, `mod_icon.py`; modelos 3D de cañones y trompeta; `export_to_mod.py` lo copia todo
  a `common/src/main/resources` y el ícono del mod a `fabric/` y `neoforge/`; `gui_icons.py` genera los íconos 2D de
  cañones y trompeta a partir del render del visor).

## Pendiente
1. Que el usuario pruebe en NeoForge (Curios): reliquias puestas, ponerse y quitarse (sonido), atributos del anillo del
   berserker y la concha, la barra de revivir del collar, y la pata de la suerte. En los dos loaders, los cambios de la
   auditoría que se notan en el juego: pata en cultivos, guantelete (una carga por golpe, solo enemigos), corazón de
   creeper (solo golpes), arcos eléctricos (ahora los dibuja el cliente) y bichos de la plaga.
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
