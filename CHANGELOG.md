# Changelog

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/);
el proyecto usa [SemVer](https://semver.org/lang/es/) con la versión de Minecraft como metadato (`0.1.0+26.3`).

## [Unreleased]
### Added
- Efecto **Hedor**: daño periódico que puede matar y se contagia en cadena a 1,5 bloques; cada salto dura un 25 % menos.
- **Pociones de hedor** (normal, larga y fuerte) con versiones arrojadiza y persistente, y flechas de hedor.
- **Zombis portadores** del Hedor (16 % por defecto): Hedor infinito sin daño propio.
- Moscas alrededor de quien tiene Hedor.
- **Cañón de papas** (rústico, crafteable) y **cañón de papas a presión** (steampunk, solo en cofres de
  estructuras): modelos 3D con animación de carga, retroceso y nube de Hedor con papa venenosa.
- **Trompeta de cobre**: cono de sonido que aturde, con eco contra paredes y bajo techo.
- **Oxidación** de cañones y trompeta en 4 etapas: pierden fuerza con el óxido; panal para encerar y hacha para raspar.
- Efecto **Aturdido**: sin movimiento, salto ni daño; los mobs pierden su objetivo.
- **Collar del murciélago** (espacio de collar de Trinkets, visible en el pecho): ecolocación con tecla (B) a 32 bloques
  y robo de vida del 6 % en golpes cuerpo a cuerpo.
- Modelos 3D que referencian texturas vanilla (mismo modelo en inventario y mano) y herramientas en `tools/model-gen`.
- Tooltips con las estadísticas de cada objeto y pestaña propia en el inventario creativo.
- Game rules: `stench_friendly_fire`, `stench_affects_source`, `stench_chain_contagion`, `stench_can_kill`,
  `stench_carrier_chance` y `trumpet_friendly_fire` (prefijo `misfitrelics:`).
- Traducciones en inglés y español.

### Dependencias
- Fabric API y Trinkets Updated 4.2.1+26.3.
