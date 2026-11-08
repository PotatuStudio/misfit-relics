# Changelog

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/);
el proyecto usa [SemVer](https://semver.org/lang/es/) con la versión de Minecraft como metadato (`0.1.0+26.3`).

## [Unreleased]
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
- Modelos 3D que referencian texturas vanilla (mismo modelo en inventario y mano) y herramientas en `tools/model-gen`.
- Tooltips con las estadísticas de cada objeto y pestaña propia en el inventario creativo.
- Game rules: `stench_friendly_fire`, `stench_affects_source`, `stench_chain_contagion`, `stench_can_kill`,
  `stench_carrier_chance` y `trumpet_friendly_fire` (prefijo `misfitrelics:`).
- Traducciones en inglés y español (todas las variantes: España, Chile, México, Argentina, etc.).

### Dependencias
- Fabric API y Trinkets Updated 4.2.1+26.3.
