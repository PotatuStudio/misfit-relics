# Misfit Relics

Lo olvidado de Minecraft convertido en equipo RPG. Un mod de **PotatuStudio** para Minecraft Java **26.3** (Fabric).

## v0.1: Hedor (en desarrollo)
- **Efecto Hedor:** daño periódico que **se contagia en cadena** a las entidades cercanas, aliadas o enemigas. Puede matar.
- **Poción de hedor:** se destila con papa venenosa, con versiones arrojadiza y persistente.
- **Cañón de papas:** se carga manteniendo clic derecho (1 s) y dispara al soltar. El efecto depende de la munición (papa: daño; papa venenosa: daño y nube de Hedor). Receta: 4 lingotes de cobre, pistón y pedernal (`CCC` / `CPF`). 250 usos, se repara con hierro.
- **Portadores:** algunos zombis aparecen con Hedor.

## v0.2: Trompeta de cobre (en desarrollo)
- **Cono de sonido** que **aturde** (efecto Aturdido: sin moverse, sin saltar, golpes sin daño; los mobs pierden su objetivo) y empuja un poco.
- **Eco:** contra una pared el aturdimiento dura un 50 % más; bajo techo, un 25 % más.
- **Oxidación: mientras mejor cuidada, mejor funciona.** Cada uso tiene un 4 % de probabilidad de oxidarla. Como en un instrumento real, el óxido apaga el sonido: llega menos lejos, se dispersa más y aturde menos. Cada etapa suena distinto (sonidos de trompeta de 26.1).

  | Etapa | Alcance | Cono | Aturdimiento | Enfriamiento |
  |---|---|---|---|---|
  | Cobre | 10 | 60° | 2 s | 3 s |
  | Expuesto | 9 | 70° | 1,5 s | 3 s |
  | Desgastado | 7 | 80° | 1 s | 3,5 s |
  | Oxidado | 5 | 90° | 0,6 s | 4 s |
- **Mantenimiento** (agachado, con la herramienta en la otra mano): el **hacha** raspa una etapa de óxido (o quita la cera) y la recupera; el **panal** la encera para que no se siga oxidando.
- Receta: 4 lingotes de cobre y un bloque musical (`  C` / `CNC` / `  C`). 200 usos, se repara con cobre.

### Game rules
| Regla | Por defecto | Qué hace |
|---|---|---|
| `misfitrelics:stench_friendly_fire` | `true` | El Hedor daña a los aliados (mascotas, aldeanos, golems, jugadores del equipo). |
| `misfitrelics:stench_affects_source` | `true` | El Hedor afecta a quien lo lanzó. |
| `misfitrelics:stench_chain_contagion` | `true` | El contagio sigue en cadena (cada salto dura un 25 % menos). |
| `misfitrelics:stench_can_kill` | `true` | El Hedor puede matar (si es `false`, deja con medio corazón). |
| `misfitrelics:stench_carrier_chance` | `2` | Porcentaje de zombis, husks y drowned que aparecen como portadores. |
| `misfitrelics:trumpet_friendly_fire` | `true` | La trompeta de cobre aturde a los aliados. |

## Requisitos
Minecraft 26.3, Fabric Loader 0.20.1 o superior, Fabric API y Java 25.

## Desarrollo
```sh
./gradlew runClient
./gradlew runServer
./gradlew build
```

## Licencia
[MIT](LICENSE)
