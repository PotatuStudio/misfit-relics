# Changelog

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/);
el proyecto usa [SemVer](https://semver.org/lang/es/) con la versión de Minecraft como metadato (`0.1.0+26.3`).

## [Unreleased]

## [0.1.1] - 2026-11-22
### Fixed
- **Guantes de seda, guantes de la viuda y guantelete eléctrico** puestos: los dedos apuntan hacia abajo y cada mano
  es el espejo de la otra (antes se veían al revés y con los dos pulgares al mismo lado).

## [0.1.0] - 2026-11-21
Primera beta pública.
### Changed
- Todas las reliquias aparecen el doble de seguido: cada probabilidad de cofre se duplicó, también la del collar del
  rey murciélago en el warden (10 %).
- **Anillo del berserker:** +200 % de daño (antes +150 %).

### Fixed (auditoría del 2026-11-19, ver docs/AUDITORIA.md)
- La **pata de la suerte** funciona con los cultivos maduros; el jackpot de la maldita ya no pierde ítems en los cofres.
- Un **zombi portador** sigue siéndolo aunque le vuelva a llegar el Hedor.
- **Guantelete eléctrico**: carga una vez por golpe (los barridos no suman), solo con golpes cuerpo a cuerpo y solo contra
  enemigos; la electrocución ya no alcanza a animales pacíficos, mascotas ni soportes de armadura.
- **Corazón de creeper**: solo reacciona a golpes de alguien (no a caídas, fuego ni hambre).
- La **embestida** tiene su propio tipo de daño (no carga el guantelete ni roba vida con el collar).
- Las reliquias de movilidad y el corazón ya no fallan al cambiar a un mundo con menos tiempo jugado.
- Recargas de la embestida y la ecolocación guardadas con el mundo (no se reinician al reconectar ni al morir).
- Bichos de la plaga: no se meten en la piedra ni apuntan al dueño, y su tiempo de vida sobrevive a recargar el mundo.
- Una ráfaga del breeze por salto también en el servidor; la trompeta deja de sonar al cambiar de dimensión.
- Memoria: el estado de las reliquias se limpia al desconectarse y al cerrar el mundo.
- Rendimiento: los arcos eléctricos se dibujan en el cliente (un paquete por arco); flechas guiadas, botas de magma y
  concha hacen menos trabajo por tick.

### Added
- Efecto **Hedor**: daño periódico que puede matar y se contagia en cadena a 1,5 bloques; cada salto dura un 25 % menos.
- **Pociones de hedor** (normal, larga y fuerte) con versiones arrojadiza y persistente, y flechas de hedor.
- **Zombis portadores** del Hedor (16 % por defecto, también desde huevos y spawners): Hedor infinito sin daño propio.
- Moscas (pocas) alrededor de quien tiene Hedor.
- **Cañón de papas** (rústico, crafteable) y **cañón de papas a presión** (steampunk, solo en cofres de
  estructuras): modelos 3D con animación de carga, retroceso y nube de Hedor con papa venenosa.
- **Trompeta de cobre**: arma de 6 de daño cuyos golpes aturden (con eco contra paredes y bajo techo);
  con clic derecho toca melodías al azar.
- **Oxidación** de cañones y trompeta en 4 etapas: pierden fuerza con el óxido; panal para encerar y hacha para raspar.
- Efecto **Aturdido**: sin movimiento, salto ni daño; los mobs pierden su objetivo.
- **Collar del murciélago** (espacio de collar de Trinkets; se ve puesto como amuleto en el pecho): ecolocación con tecla (B) a 32 bloques
  robo de vida del 10 % en golpes cuerpo a cuerpo y **murciélago aliado** que muerde a tus enemigos
  (revive a los 60 s si muere, con barra en el collar).
- **Collar del rey murciélago** (coleccionable, ciudad antigua 1,5 % o warden 5 %): invoca al rey murciélago,
  más grande, con mordisco de 6, robo de vida del 25 % y chillido aturdidor.
- El murciélago aliado tiene rutinas: pasea cazando insectos, de día busca sombra y se cuelga boca abajo.
- Brillo animado en los íconos de anillos, patas y collares.
- **Patas de conejo** (espacio de cinturón de Trinkets), en minería, mobs, cofres de estructuras y pesca:
  la **de la suerte** (25 % de botín ×2, nunca pierde) y la **maldita** (15 % de jackpot ×4 a ×10, 10 % de perderlo todo).
- **Anillo de la plaga** y **Anillo del festín** (espacios de anillo de Trinkets): la plaga suelta silverfish o endermites
  aliados al matar; el festín los absorbe al tocarlos y cura 2 corazones.
- Las reliquias se encuentran en cofres de estructuras temáticas (solo el cañón rústico tiene receta).
- Reliquias de combate: **guantelete eléctrico** (carga y electrocución en cadena), **corazón de creeper cargado**
  (rayo que aturde al recibir daño), **carcaj del bosque** (flechas que enredan), **carcaj del vigilante** (flechas que
  persiguen), **concha de nautilus** (bajo el agua) y **anillo del berserker** (menos vida, mucho más daño).
- Reliquias de movilidad: **guantes de seda** y **de la viuda** (escalada), **breeze en una botella** (ráfaga en el aire),
  **botas de magma** (caminar sobre lava) y **cuernos del carnero** (embestida invulnerable).
- Collar puesto: solo la cadena bajando del cuello y el dije (recorte del ícono).
- Dónde encontrar cada reliquia: línea en el tooltip y página de información en JEI (integración opcional).
- Árbol de 13 logros coleccionables.
- Modelos 3D que referencian texturas vanilla (mismo modelo en inventario y mano) y herramientas en `tools/model-gen`.
- Tooltips con las estadísticas de cada objeto y pestaña propia en el inventario creativo.
- Game rules: `stench_friendly_fire`, `stench_affects_source`, `stench_chain_contagion`, `stench_can_kill`,
  `stench_carrier_chance` y `trumpet_friendly_fire` (prefijo `misfitrelics:`).
- Traducciones en inglés y español (todas las variantes: España, Chile, México, Argentina, etc.).

- Soporte para **NeoForge** (además de Fabric), con Architectury: las reliquias van en los espacios de **Curios** y se
  ven puestas igual que con Trinkets.

### Dependencias
- Architectury API 22.0.5 o superior.
- Fabric: Fabric API y Trinkets Updated 4.2.1+26.3.
- NeoForge: NeoForge 26.3.0.101 o superior y Curios API 17.0.0+26.3.
