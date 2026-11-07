# Misfit Relics

Lo olvidado de Minecraft convertido en equipo RPG. Un mod de **PotatuStudio** para Minecraft Java **26.3** (Fabric).

## v0.1: Hedor (en desarrollo)
- **Efecto Hedor:** daño periódico que **se contagia en cadena** a las entidades cercanas, aliadas o enemigas. Puede matar.
- **Poción de hedor:** se destila con papa venenosa, con versiones arrojadiza y persistente.
- **Cañón de papas:** se carga manteniendo clic derecho (1 s) y dispara al soltar. El efecto depende de la munición (papa: daño; papa venenosa: daño y nube de Hedor). Receta: 4 lingotes de cobre, pistón y pedernal (`CCC` / `CPF`). 250 usos, se repara con hierro.
- **Portadores:** algunos zombis aparecen con Hedor.

### Game rules
| Regla | Por defecto | Qué hace |
|---|---|---|
| `misfitrelics:stench_friendly_fire` | `true` | El Hedor daña a los aliados (mascotas, aldeanos, golems, jugadores del equipo). |
| `misfitrelics:stench_affects_source` | `true` | El Hedor afecta a quien lo lanzó. |
| `misfitrelics:stench_chain_contagion` | `true` | El contagio sigue en cadena (cada salto dura un 25 % menos). |
| `misfitrelics:stench_can_kill` | `true` | El Hedor puede matar (si es `false`, deja con medio corazón). |
| `misfitrelics:stench_carrier_chance` | `2` | Porcentaje de zombis, husks y drowned que aparecen como portadores. |

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
