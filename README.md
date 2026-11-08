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
Arma cuerpo a cuerpo e instrumento.
- **Golpe:** 6 de daño (velocidad de espada). Se puede encantar con filo, castigo, perdición de los artrópodos, empuje, irrompibilidad y reparación.
- **Pasiva:** cada golpe **aturde** (sin moverse, sin saltar, golpes sin daño; los mobs pierden su objetivo). Si el objetivo ya está aturdido, no se renueva.
- **Eco:** si el objetivo queda contra una pared, el aturdimiento dura un 50 % más; bajo techo, un 25 % más.
- **Clic derecho:** toca una melodía al azar (¡A la carga!, dut dut, trombón triste, bocinazo y escala de calentamiento). Solo es música.
- Cada etapa de oxidación suena distinto (sonidos de trompeta de 26.1).

  | Etapa | Aturdimiento por golpe |
  |---|---|
  | Cobre | 2 s |
  | Expuesto | 1,5 s |
  | Desgastado | 1 s |
  | Oxidado | 0,6 s |
- **Dónde:** bóvedas de las cámaras de desafío (12 %) y sus cofres de suministros (4 %), casi siempre con poco óxido.

## Oxidación (cañones y trompeta)
**Mientras mejor cuidados, mejor funcionan.** Se oxidan con el uso y pierden fuerza. Agachado, con la herramienta en la otra mano: el **hacha** raspa una etapa de óxido (o quita la cera) y el **panal** los encera para que no se sigan oxidando.

## Collar del murciélago
Requiere **[Trinkets Updated](https://modrinth.com/mod/trinkets-updated)**: va en el espacio *collar* y se ve puesto: un amuleto del murciélago colgando de un cordón en el pecho.
- **Ecolocación** (tecla **B**, configurable): marca con contorno a todas las criaturas en 32 bloques durante 6 s, incluso a través de paredes. Recarga de 10 s.
- **Robo de vida:** los golpes cuerpo a cuerpo recuperan un 10 % del daño hecho.
- **Murciélago aliado:** mientras llevas el collar, un murciélago revolotea sobre tu cabeza y **muerde a lo que golpeas o a quien te golpea** (3 de daño por mordisco; cada mordisco te cura con el robo de vida). No puedes herirlo tú. Si muere, el collar muestra una **barra morada** y vuelve a los **60 s**.
- **Dónde:** cofres de la ciudad antigua (10 %).

## Patas de conejo
Requieren **[Trinkets Updated](https://modrinth.com/mod/trinkets-updated)**: van en el espacio *cinturón* (se lleva una u otra) y cuelgan de la cadera.

| | **Pata de conejo de la suerte** | **Pata de conejo maldita** |
|---|---|---|
| Efecto | **25 %** de botín **×2**. Nunca pierde. | **15 %** de jackpot **×4 a ×10**, pero **10 %** de **perderlo todo**. |
| Dónde | Pirámides del desierto (12 %), cofres secretos de campamentos abandonados (10 %) y suministros de naufragios (5 %) | **Nether:** tesoros de bastión (20 %) y, rara vez, portales en ruinas (3 %) |

- Aplican en **minerales y cultivos maduros** que rompes (con toque de seda no aplican), **mobs que matas**, **cofres de estructuras** la primera vez que se abren y **pesca**.

## Anillos de la plaga y del festín
Requieren **[Trinkets Updated](https://modrinth.com/mod/trinkets-updated)**: van en los espacios de *anillo* (uno en cada mano). Por separado son modestos; juntos cierran un ciclo de sustento en combate.

| | **Anillo de la plaga** | **Anillo del festín** |
|---|---|---|
| Efecto | Al matar un enemigo, **25 %** de soltar de 1 a 3 bichos, cada uno al azar entre silverfish y endermite, **aliados**: pelean contra los enemigos cercanos 8 s, nunca contra ti ni tus aliados, vuelven a ti y se deshacen a los 20 s. | Al **tocar** un silverfish o un endermite (aliado o salvaje) lo **absorbes** y te cura **2 corazones**. |
| Dónde | **Fortaleza:** biblioteca (15 %), pasillos y cruces (8 %) | **End:** tesoros de las ciudades del End (12 %) |

## Cómo se consiguen
Como en *Artifacts*, las reliquias **no se fabrican**: se encuentran en cofres, y mientras más fuertes, más difícil la estructura. Solo el **cañón rústico** tiene receta, y las **pociones de hedor** se destilan.

## Game rules
| Regla | Por defecto | Qué hace |
|---|---|---|
| `misfitrelics:stench_friendly_fire` | `true` | El Hedor daña a los aliados (mascotas, aldeanos, golems, jugadores del equipo). |
| `misfitrelics:stench_affects_source` | `true` | El Hedor afecta a quien lo lanzó. |
| `misfitrelics:stench_chain_contagion` | `true` | El contagio sigue en cadena (cada salto dura un 25 % menos). |
| `misfitrelics:stench_can_kill` | `true` | El Hedor puede matar (si es `false`, deja con medio corazón). |
| `misfitrelics:stench_carrier_chance` | `16` | Porcentaje de zombis, husks y drowned que aparecen como portadores (también desde huevos, `/summon` y spawners). |
| `misfitrelics:trumpet_friendly_fire` | `true` | Los golpes de la trompeta de cobre aturden a los aliados. |

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
