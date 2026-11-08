# Misfit Relics

Lo olvidado de Minecraft convertido en equipo RPG. Un mod de **PotatuStudio** para Minecraft Java **26.3** (Fabric).

Los objetos son **modelos 3D** que usan las **texturas del propio juego** (cobre, pistón, yunque, cuarzo…):
se ven igual en el inventario y en la mano, y cambian con los resource packs.

## Hedor
- **Efecto Hedor:** daño periódico que **se contagia en cadena** a las entidades cercanas, aliadas o enemigas. Puede matar. Lo acompañan moscas.
- **Poción de hedor:** se destila con papa venenosa, con versiones arrojadiza, persistente y flechas.
- **Portadores:** el 16 % de los zombis aparece con Hedor.

## Cañón de papas
Se carga manteniendo clic derecho y dispara al soltar, con retroceso. Papa: daño y empuje; papa venenosa: además, nube de Hedor de 3 bloques.

| | **Cañón de papas** (rústico) | **Cañón de papas a presión** (steampunk) |
|---|---|---|
| Cómo se consigue | Receta `CCC` / `SPF` / ` T ` (cobre, cuerda, pistón, pedernal, palo) | Solo en cofres: mazmorras, minas, templos, puestos de saqueadores, naufragios y campamentos abandonados |
| Carga | 0,5 s | 0,4 s |
| Daño (cobre → oxidado) | 6 / 5 / 4 / 3 | 8 / 7 / 6 / 5 |
| Durabilidad | 250 | 400 |

## Trompeta de cobre
- **Cono de sonido** que **aturde** (sin moverse, sin saltar, golpes sin daño; los mobs pierden su objetivo) y empuja un poco.
- **Eco:** contra una pared el aturdimiento dura un 50 % más; bajo techo, un 25 % más.
- Cada etapa de oxidación suena distinto (sonidos de trompeta de 26.1).

  | Etapa | Alcance | Cono | Aturdimiento | Enfriamiento |
  |---|---|---|---|---|
  | Cobre | 10 | 60° | 2 s | 3 s |
  | Expuesto | 9 | 70° | 1,5 s | 3 s |
  | Desgastado | 7 | 80° | 1 s | 3,5 s |
  | Oxidado | 5 | 90° | 0,6 s | 4 s |
- Receta: 4 lingotes de cobre y un bloque musical (`  C` / `CNC` / `  C`).

## Oxidación (cañones y trompeta)
**Mientras mejor cuidados, mejor funcionan.** Se oxidan con el uso y pierden fuerza. Agachado, con la herramienta en la otra mano: el **hacha** raspa una etapa de óxido (o quita la cera) y el **panal** los encera para que no se sigan oxidando.

## Collar del murciélago
Requiere **[Trinkets Updated](https://modrinth.com/mod/trinkets-updated)**: va en el espacio *collar* y se ve en el pecho.
- **Ecolocación** (tecla **B**, configurable): marca con contorno a todas las criaturas en 32 bloques durante 6 s, incluso a través de paredes. Recarga de 10 s.
- **Robo de vida:** los golpes cuerpo a cuerpo recuperan un 6 % del daño hecho.
- Receta: 5 cuerdas, 2 membranas de phantom y un fragmento de eco (`SSS` / `S S` / `MEM`).

## Game rules
| Regla | Por defecto | Qué hace |
|---|---|---|
| `misfitrelics:stench_friendly_fire` | `true` | El Hedor daña a los aliados (mascotas, aldeanos, golems, jugadores del equipo). |
| `misfitrelics:stench_affects_source` | `true` | El Hedor afecta a quien lo lanzó. |
| `misfitrelics:stench_chain_contagion` | `true` | El contagio sigue en cadena (cada salto dura un 25 % menos). |
| `misfitrelics:stench_can_kill` | `true` | El Hedor puede matar (si es `false`, deja con medio corazón). |
| `misfitrelics:stench_carrier_chance` | `16` | Porcentaje de zombis, husks y drowned que aparecen como portadores. |
| `misfitrelics:trumpet_friendly_fire` | `true` | La trompeta de cobre aturde a los aliados. |

## Requisitos
Minecraft 26.3, Fabric Loader 0.20.1 o superior, Fabric API, [Trinkets Updated](https://modrinth.com/mod/trinkets-updated) 4.2.1 o superior y Java 25.

## Desarrollo
```sh
./gradlew runClient
./gradlew runServer
./gradlew build
```
Los modelos 3D se generan y se revisan con las herramientas de [`tools/model-gen`](tools/model-gen/README.md).

## Licencia
[MIT](LICENSE)
