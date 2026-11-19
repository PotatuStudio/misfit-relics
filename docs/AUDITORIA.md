# Auditoría (2026-11-19)

Revisión de la lógica del mod (no del uso de Fabric API, que se reemplazó en el port). Las líneas citadas son del commit
`262ff03`, antes del port: los archivos ahora están en `common/src/main/java/...` con el mismo nombre de clase.
**[JUEGO]** = el arreglo cambia el comportamiento en el juego: se aprueba antes de tocarlo. *(sospecha)* = sin
comprobar en el juego.

## 1. Bugs
### Alta
1. **Tiempo del mundo anterior en mapas estáticos**: `Mobility` (CLIMB_SUPPRESSED, LAST_DASH, SAFE_FALL), `CombatRelics`
   (HEART_LAST, CHARGES) guardan `getGameTime()` y nunca se limpian. Tras cambiar a un mundo con menos tiempo: la viuda
   no trepa, el corazón de creeper no se activa, el servidor rechaza la ráfaga del breeze (y hay daño de caída), las
   chispas del guantelete no se apagan. Arreglo: `ServerState.perPlayer(...)` y tratar una resta negativa como "pasó".
2. **[JUEGO] Un zombi portador pierde esa condición** si lo vuelve a tocar el Hedor (`LivingEntityMixin:42` escribe
   `StenchData(source, true, false)` siempre, aunque el efecto ya exista o `addEffect` falle). Arreglo: conservar
   `carrier` y escribir solo si `addEffect` devuelve true.
3. **[JUEGO] La pata de la suerte nunca aplica a los cultivos** (`LuckyRabbitFoot:367`): el chequeo de "toque de seda"
   compara con `block.asItem()`, que en los cultivos es la semilla o el fruto, siempre presente en el botín. Arreglo:
   aplicar ese chequeo solo a las menas.

### Media
4. **Mapas compartidos entre el hilo del cliente y el del servidor** en un jugador: `MobilityClient` escribe
   CLIMB_SUPPRESSED que lee el servidor; `EntityFireMixin` lee MAGMA_GRACE en el render. Además en servidor dedicado
   el servidor nunca recibe la supresión ni el cliente la gracia. Arreglo: estado separado por lado.
5. **[JUEGO] El guantelete se dispara con barridos y con la embestida** (`CombatRelics:103`): un barrido a 4 mobs suma
   4 cargas y lanza 4 cadenas; la embestida además roba vida con el collar. Arreglo: solo `PLAYER_ATTACK` sin barrido y
   un tipo de daño propio para la embestida.
6. **[JUEGO] La electrocución alcanza a pacíficos y soportes de armadura** (`CombatRelics:129-131`), y a tu propio lobo
   si lo golpeas. Arreglo: exigir `Enemy` (o el último atacado) y excluir `ArmorStand`.
7. **[JUEGO] El corazón de creeper reacciona a cualquier daño** (caída, hambre, fuego): rayo sobre el más cercano en 8
   bloques, aunque el tooltip dice "sobre quien te pegó". Arreglo: exigir atacante vivo (o cambiar el texto).
8. **Bichos de la plaga con IA vanilla**: el silverfish puede meterse en la piedra *(sospecha)* y entre ticks vuelve a
   apuntar al dueño (el daño se bloquea, pero se ve).
9. **El tiempo de vida de los bichos se reinicia al recargar** (`tickCount` no se guarda; el dueño sí). Arreglo: guardar
   el tick de nacimiento en `EntityData`.
10. **[JUEGO] El jackpot de la pata maldita puede perder ítems en cofres** *(sospecha)*: ×4–×10 de ítems no apilables
    supera las 27 casillas y `LootTable.fill` descarta lo que no cabe.
11. **Recargas que se pierden al reconectar**: embestida y ecolocación usan `ItemCooldowns` vanilla (3 s y 10 s).
12. **El servidor no valida la ráfaga del breeze** (una por salto) y la embestida empuja en el cliente aunque el servidor
    la rechace. Baja.
13. Menores: la trompeta sigue sonando en la dimensión anterior; la ecolocación no se limpia al cambiar de dimensión
    (ids reutilizados); `MobilityClient` no reinicia su estado al desconectarse; la ecolocación marca espectadores e
    invisibles *(sospecha)*.

## 2. Fugas de memoria
- Mapas por UUID que nunca se vacían: `Mobility` (RAMS, MAGMA_GRACE, LAST_DASH, CLIMB_SUPPRESSED, SAFE_FALL) y
  `CombatRelics` (CHARGES, HEART_LAST). Origen del bug 1.
- Referencias que retienen el mundo cerrado hasta el siguiente tick: `BatCollar` (entidades), `PlagueRings` (Mob),
  `CombatRelics` (`Arc` con `ServerLevel`, `Homing.target`), `TrumpetSongs` (`Performance` con `ServerLevel`).
  Arreglo: `ServerState.clearOnStop(...)`.

## 3. Rendimiento
- Consultas de accesorios repetidas sin caché: **resuelto en el port** (`WornRelics.isWearing` cachea por tick).
- Flechas guiadas: cada flecha sin objetivo busca cada tick en una caja de 40³ con `hasLineOfSight`. Buscar cada 4–5 ticks.
- Arcos eléctricos: `sendParticles` por cada segmento de 0,26 bloques durante 6 ticks (cientos de paquetes por tick).
  Arreglo: un payload (origen, destino) y dibujar el arco en el cliente.
- Resincronización cada tick *(probable)*: el modificador de atributo de las botas de magma y la gracia del delfín de la
  concha se reaplican cada tick. Renovar solo si falta o queda poco.
- `ModLoot.sources` reconstruye mapas en cada tooltip. Calcularlo una vez.

## 4. APIs deprecadas
Sin hallazgos evidentes. Pendiente compilar con `-Xlint:deprecation`.

## 5. Código muerto y a medio hacer
- `CombatRelics.chargeOf` sin uso; `ARC_TICKS` público sin motivo; "más adelante" en `SpudCannonItem:43`.
- Dos enums de oxidación casi iguales (`TrumpetStage`, `CopperStage`) con distinto comportamiento en los extremos.
- **[JUEGO]** `StenchEffect:68` `40 >> amplifier`: con amplificador 32–36 el desplazamiento se enmascara y el intervalo
  vuelve a 40. Arreglo: `Math.min(amplifier, 31)`.

## 6. Pruebas
Hay una prueba de humo desde el port (`fabric/src/gametest`). Faltan:
- Unitarias (JUnit en `common`): números de `CannonDesign` por etapa, `next/previous` de las etapas, intervalo del Hedor,
  `Mobility.lavaSurface`, reparto de `LuckyRabbitFoot.multiply`, melodías ordenadas y con tonos 0–24, ida y vuelta de
  `StenchData.CODEC`, cada reliquia con fuente en `ModLoot.sources`, claves de idioma iguales en en_us y es_es.
- De juego: pata sobre trigo maduro (bug 3), portador con papa venenosa (bug 2), dos mundos con distinto tiempo (bug 1),
  barrido con guantelete, embestida, botas sobre lava que corre, flecha guiada, bichos.

## 7. Diseño
- `isWearing` copiado cuatro veces: **resuelto en parte** (las copias delegan en `WornRelics`; falta borrarlas).
- Filtros de enemigos repetidos (`CombatRelics.isEnemyOf`, `AlliedBat.isValidTarget`, `PlagueRings.isValidTarget`,
  `Mobility`): pasarlos a `Allies` como en Mythic. `CombatItem.fmt` y `formatChance` de JEI: un `TooltipNumbers`.
- Clases grandes: `CombatRelics` (410 líneas, 5 reliquias y el dibujo de arcos), `Mobility` (301), `AlliedBat` (396).
- Records con `int[]`/`long[]` mutables (`Charge`, `Arc`, `Ram`, `Performance`); números mágicos sin nombre en
  `CombatRelics`, `Mobility`, `PlagueRings` y `AlliedBat`.
- Cantidad de líneas de tooltip fija en `MobilityItem.Kind` y `CombatItem.Kind` (frágil frente a los idiomas).
