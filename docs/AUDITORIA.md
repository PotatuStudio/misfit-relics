# Auditoría (2026-11-19)

> **Estado (2026-11-19, misma sesión): corregida.** El usuario aprobó todo, también lo que cambia el juego. Lo que quedó
> sin hacer está marcado como **Pendiente**, con el motivo. Verificación: `./gradlew build`, `./gradlew :common:test`
> (12 pruebas) y `./gradlew :fabric:runClientGameTest` (46 comprobaciones) en verde; `:neoforge:runServer` arranca.
> Durante la corrección apareció un bug del port: `LootTableMixin` no estaba en `misfitrelics.mixins.json` (en el commit
> del port la pata de la suerte no hacía nada) y en NeoForge su punto de inyección es otro. Ya está arreglado.

Revisión de la lógica del mod. Las líneas citadas son del commit `262ff03`, antes del port: los archivos ahora están en
`common/src/main/java/...` con el mismo nombre de clase. **[JUEGO]** = el arreglo cambia el comportamiento en el juego.
*(sospecha)* = sin comprobar en el juego.

## 1. Bugs
### Alta
1. **Tiempo del mundo anterior en mapas estáticos**: `Mobility` (CLIMB_SUPPRESSED, LAST_DASH, SAFE_FALL) y
   `CombatRelics` (HEART_LAST, CHARGES) guardaban `getGameTime()` y nunca se limpiaban. Tras cambiar a un mundo con
   menos tiempo: la viuda no trepaba, el corazón de creeper no se activaba, el servidor rechazaba la ráfaga del breeze
   (con daño de caída) y las chispas del guantelete no se apagaban.
   **Hecho**: `ServerState.perPlayer` en `Mobility` y `CombatRelics`; una resta negativa cuenta como vencida.
2. **[JUEGO] Un zombi portador perdía esa condición** si lo volvía a tocar el Hedor (`LivingEntityMixin` escribía
   `StenchData(source, true, false)` siempre, aunque el efecto ya existiera o `addEffect` fallara).
   **Hecho**: el dato se escribe al final de `addEffect`, solo si el efecto entró, y el portador se conserva.
3. **[JUEGO] La pata de la suerte nunca aplicaba a los cultivos**: el chequeo de "toque de seda" comparaba con
   `block.asItem()`, que en los cultivos es la semilla o el fruto. **Hecho**: ese chequeo es solo para menas.

### Media
4. **Mapas compartidos entre el hilo del cliente y el del servidor** en un jugador (supresión de trepar, gracia de
   magma). **Hecho**: la supresión es solo del cliente (`CLIMB_SUPPRESSED_CLIENT`) y la gracia solo se lee en el servidor.
5. **[JUEGO] El guantelete se disparaba con barridos y con la embestida** (y la embestida robaba vida con el collar).
   **Hecho**: solo golpes `PLAYER_ATTACK`, una carga por tick (un barrido suma una) y la embestida usa su propio tipo de
   daño `misfitrelics:ram`, que tampoco roba vida.
6. **[JUEGO] La electrocución alcanzaba a pacíficos, soportes de armadura y a tu propio lobo**.
   **Hecho**: `Allies.isEnemy` (monstruos, lo que te tiene de objetivo, lo último que golpeaste y jugadores no aliados;
   nunca pacíficos, aliados ni soportes). El golpe inicial también lo exige.
7. **[JUEGO] El corazón de creeper reaccionaba a cualquier daño** (caída, hambre, fuego).
   **Hecho**: exige que el daño venga de alguien, como dice el tooltip.
8. **Bichos de la plaga con IA vanilla** (el silverfish podía meterse en la piedra; entre ticks apuntaban al dueño).
   **Hecho**: al cargarse se les quitan los objetivos propios y el meterse en la piedra (`mixin/MobAccessor`).
9. **El tiempo de vida de los bichos se reiniciaba al recargar**. **Hecho**: `EntityData` `plague_born`.
10. **[JUEGO] El jackpot de la pata maldita podía perder ítems en cofres** (más pilas que casillas).
    **Hecho**: en cofres el multiplicador baja hasta que todo quepa en 27 casillas.
11. **Recargas que se perdían al reconectar** (embestida y ecolocación). **Hecho**: `relic/RelicCooldowns` (como en
    Mythic) las guarda con el mundo y las repone al entrar y al reaparecer.
12. **El servidor no validaba la ráfaga del breeze**. **Hecho**: una ráfaga por salto también en el servidor. La
    embestida ya revisaba cuernos y recarga en el cliente antes de empujar.
13. Menores. **Hecho**: la trompeta deja de sonar si cambias de dimensión; la ecolocación se limpia al reaparecer o salir
    y no marca espectadores (los invisibles sí: es eco); `MobilityClient` se reinicia al salir; el murciélago aliado se
    va con su dueño al desconectarse.

## 2. Fugas de memoria
Mapas por UUID que nunca se vaciaban (`Mobility`, `CombatRelics`) y referencias que retenían el mundo cerrado
(`BatCollar`, `PlagueRings`, los arcos de `CombatRelics`, `TrumpetSongs`). **Hecho**: todo con `ServerState`; los arcos
ya no guardan entidades ni mundos en el servidor.

## 3. Rendimiento
- Consultas de accesorios sin caché. **Hecho** en el port (`WornRelics.isWearing` cachea por tick).
- Flechas guiadas buscando cada tick en 40³ con línea de visión. **Hecho**: cada 4 ticks.
- Arcos eléctricos: cientos de paquetes de partículas por tick. **Hecho**: un `ArcPayload` por arco y el cliente lo
  dibuja (`client/ArcClient`), igual que antes.
- Botas de magma y concha reaplicando atributo y efecto cada tick. **Hecho**: solo al cambiar o cuando queda poco.
- `ModLoot.sources` reconstruía mapas en cada tooltip. **Hecho**: se calcula una vez.

## 4. APIs deprecadas
Sin hallazgos evidentes.

## 5. Código muerto
- `CombatRelics.chargeOf` sin uso y `ARC_TICKS` público. **Hecho** (borrado y privado). La nota "más adelante" de
  `SpudCannonItem` es una idea: se deja.
- Dos enums de oxidación (`TrumpetStage`, `CopperStage`). **Se dejan**: guardan datos distintos (sonido y aturdimiento
  contra prefijo del id); una prueba fija cómo se comporta cada uno en los extremos.
- **[JUEGO]** `StenchEffect`: `40 >> amplifier` con amplificador 32–36 volvía a 40. **Hecho**: `Math.min(amplifier, 31)`,
  con prueba.

## 6. Pruebas
**Hecho**: JUnit en `common` (`./gradlew :common:test`): intervalo del Hedor, códec de `StenchData`, pilas de la pata,
números del cañón por etapa, extremos de las etapas, melodías y claves de idioma (mismas claves en los dos idiomas, sin
huecos en los tooltips, mismos marcadores). La prueba de juego comprueba además el portador del Hedor y que cada
reliquia tenga dónde encontrarse.
**Pendiente** (piden escenarios con azar o varios mundos): pata sobre trigo, dos mundos con distinto tiempo, barrido
con guantelete, embestida, lava que corre, flecha guiada y bichos.

## 7. Diseño
- `isWearing` copiado cuatro veces. **Hecho**: todo usa `WornRelics.isWearing`.
- Filtros de enemigos y formato de números repetidos. **Hecho**: `Allies.isEnemy` para los efectos de área (el
  murciélago y los bichos conservan sus reglas, que son distintas a propósito) y `relic/TooltipNumbers`.
- Botas de magma con la regla de la lava repetida en el mixin. **Hecho**: usa `Mobility.walksOnLava`.
- Records con arreglos mutables y números mágicos. **Hecho** en `CombatRelics`, `Mobility` y `TrumpetSongs`.
  **Pendiente** en `AlliedBat` y `PlagueRings`.
- Cantidad fija de líneas de tooltip. **Hecho**: se muestran todas las `tooltip.N` que tenga el idioma.
- Clases grandes (`CombatRelics`, `Mobility`, `AlliedBat`). **Pendiente**, como D5 en Mythic: el dibujo de los arcos ya
  salió de `CombatRelics`; partir el resto queda para después.
