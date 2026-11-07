# Changelog

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/);
el proyecto usa [SemVer](https://semver.org/lang/es/) con la versión de Minecraft como metadato (`0.1.0+26.3`).

## [Unreleased]
### Added
- Efecto **Hedor**: daño periódico que puede matar y se contagia en cadena a 1,5 bloques; cada salto dura un 25 % menos.
- **Pociones de hedor** (normal, larga y fuerte) con versiones arrojadiza y persistente, y flechas de hedor.
- **Zombis portadores** del Hedor (16 % por defecto): Hedor infinito sin daño propio.
- **Cañón de papas** con modelo 3D y animación de carga: 0,5 s de carga, retroceso, 6 de daño y empuje.
  La papa venenosa además deja una nube de Hedor de 3 bloques.
- **Trompeta de cobre**: cono de sonido que aturde, con eco contra paredes y bajo techo.
  Funciona mejor mientras menos oxidada esté; panal para encerar y hacha para raspar.
- Efecto **Aturdido**: sin movimiento, salto ni daño; los mobs pierden su objetivo.
- **Collar del murciélago** (espacio de collar de Trinkets): ecolocación con tecla (B) y robo de vida del 6 % en golpes cuerpo a cuerpo.
- Game rules: `stench_friendly_fire`, `stench_affects_source`, `stench_chain_contagion`, `stench_can_kill`,
  `stench_carrier_chance` y `trumpet_friendly_fire` (prefijo `misfitrelics:`).
- Traducciones en inglés y español.

### Dependencias
- Fabric API y Trinkets Updated 4.2.1+26.3.
