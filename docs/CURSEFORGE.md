# Publicar en CurseForge

Guía para el formulario de [authors.curseforge.com](https://authors.curseforge.com). Los textos que ve el público van en
inglés porque es la audiencia de CurseForge; el mod trae `en_us` y `es_es`.

## 1. Crear el proyecto (Create Project)
| Campo | Qué poner |
|---|---|
| Game / Class | Minecraft → **Mods** |
| Name | `Misfit Relics` |
| Summary | `The forgotten bits of Minecraft turned into RPG gear: potato cannons, a stunning copper trumpet, a bat companion and more.` |
| Main category | **Adventure and RPG** |
| Additional categories | Armor, Tools, and Weapons · Mobs |
| License | **MIT License** |
| Logo | `media/icon-512.png` (512×512, CurseForge lo reduce a 400×400) |
| Issues / Source / Wiki | Vacíos mientras el repo sea privado (un link roto es motivo de rechazo) |
| Allow comments | Sí |
| Distribution (modpacks/apps de terceros) | Sí |

### Description (Markdown)
```markdown
# Misfit Relics

The forgotten and the weird of Minecraft, turned into RPG equipment. Relics are **found in structure chests**, not
crafted — the stronger the relic, the harder the structure.

## Stench
A damage-over-time effect that **spreads in a chain** to nearby entities, friend or foe. Brew it from a poisonous potato
(splash, lingering and tipped arrows included). Some zombies spawn as **carriers**.

## Potato cannons & copper trumpet
- **Potato cannon** (craftable) and **pressurised potato cannon** (chests only): hold to charge, release to fire, with
  recoil. Poisonous potatoes leave a Stench cloud.
- **Copper trumpet**: a melee weapon whose hits **stun**, longer against walls or under a roof. Right-click plays a
  random tune.
- All three **oxidise with use** and lose power: scrape them with an axe, wax them with honeycomb.
- 3D models built from vanilla textures, so they follow your resource pack.

## Accessories
- **Bat collar**: echolocation (see mobs through walls), lifesteal and a **bat companion** that hunts, perches on your
  head and bites your enemies. The rare **Bat King collar** summons a bigger, fiercer one.
- **Rabbit's feet**: lucky (double loot) or cursed (huge jackpot… or lose it all).
- **Plague and Feast rings**: summon allied silverfish and endermites, then eat them to heal.
- **Mobility**: wall-climbing gloves, a breeze in a bottle air dash, magma boots that walk on lava, ram horns dash.
- **Combat**: electric gauntlet with chain lightning, charged creeper heart, homing and entangling quivers, nautilus
  shell, berserker ring.

Every relic's tooltip says where to find it; with **JEI**, each one has a page with locations and chances.
Custom advancements and game rules to tune the Stench.

## Requirements
| | Fabric | NeoForge |
|---|---|---|
| Minecraft | 26.3 | 26.3 |
| Loader | Fabric Loader 0.20.1+ and Fabric API | NeoForge 26.3.0.101+ |
| Accessory slots | Trinkets Updated 4.2.1+ | Curios API 17.0.0+ |
| Library | Architectury API 22.0.5+ | Architectury API 22.0.5+ |

Java 25. Optional: **JEI**, **Jade**. Install it on both the client and the server.
```

## 2. Subir los archivos (Upload File)
Un archivo por loader, en el mismo proyecto. Antes: `./gradlew build` y tomar los jars **sin** `-raw` ni `-sources`.

| Campo | Fabric | NeoForge |
|---|---|---|
| Archivo | `fabric/build/libs/misfitrelics-fabric-0.1.0+26.3.jar` | `neoforge/build/libs/misfitrelics-neoforge-0.1.0+26.3.jar` |
| Display name | `Misfit Relics 0.1.0 (Fabric 26.3)` | `Misfit Relics 0.1.0 (NeoForge 26.3)` |
| Release type | **Beta** | **Beta** |
| Game version | 26.3 | 26.3 |
| Mod loader | Fabric | NeoForge |
| Java | Java 25 | Java 25 |
| Environment | Client + Server | Client + Server |
| Required dependencies | Architectury API, Fabric API, Trinkets Updated | Architectury API, Curios API |
| Optional dependencies | JEI, Jade | JEI, Jade |

### Changelog
```markdown
First public beta.
- Stench effect, potions and carrier zombies.
- Potato cannons and copper trumpet with oxidation.
- Bat collars, rabbit's feet, plague/feast rings, mobility and combat relics.
- Fabric (Trinkets Updated) and NeoForge (Curios API).
```

## 3. Después
- La moderación revisa el proyecto y cada archivo (horas a pocos días). Si rechazan, el correo dice por qué.
- Para cada versión nueva: subir `mod_version` en `gradle.properties`, agregar la sección al `CHANGELOG.md` y subir
  los dos jars otra vez.
