package io.github.potatustudio.misfitrelics.registry;

import io.github.potatustudio.misfitrelics.spud.CannonDesign;
import io.github.potatustudio.misfitrelics.trumpet.TrumpetStage;
import io.github.potatustudio.misfitrelics.util.CopperStage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import dev.architectury.event.events.common.LootEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;

/**
 * Dónde se encuentran las reliquias. Salvo el cañón rústico (crafteable) y las pociones (destilería), todo es botín:
 * mientras más fuerte la reliquia, más difícil la estructura y más baja la probabilidad, y cada una vive donde le
 * corresponde por tema (el murciélago en la ciudad antigua, la plaga en la fortaleza, el festín en el End...).
 * Cada entrada agrega un pool propio a la tabla vanilla con la probabilidad indicada por cofre.
 */
public final class ModLoot {
	/** Una reliquia en una tabla de botín: con qué probabilidad aparece y qué variantes con qué peso. */
	private record Drop(float chance, Supplier<Map<Item, Integer>> items) {
		static Drop single(float chance, Supplier<Item> item) {
			return new Drop(chance, () -> Map.of(item.get(), 1));
		}
	}

	private static final Map<ResourceKey<LootTable>, List<Drop>> DROPS = Map.ofEntries(
			// Cañón a presión (steampunk): lleva tiempo abandonado, suele aparecer algo oxidado.
			Map.entry(BuiltInLootTables.SIMPLE_DUNGEON, List.of(pressureCannon(0.08f),
					// Corazón de creeper cargado: en mazmorras y pirámides, donde abundan los creepers.
					Drop.single(0.06f, () -> ModItems.CHARGED_CREEPER_HEART.get()))),
			Map.entry(BuiltInLootTables.ABANDONED_MINESHAFT, List.of(pressureCannon(0.05f),
					// Guantes de seda: entre las arañas de cueva de las minas.
					Drop.single(0.08f, () -> ModItems.SILK_GLOVES.get()))),
			// Guantes de la viuda: en la mansión del bosque, más difíciles.
			Map.entry(BuiltInLootTables.WOODLAND_MANSION, List.of(Drop.single(0.06f, () -> ModItems.WIDOW_GLOVES.get()),
					// Carcaj del vigilante: en la mansión, donde vigilan los illagers.
					Drop.single(0.05f, () -> ModItems.WATCHER_QUIVER.get()))),
			// Botas de magma: en las fortalezas del Nether, cerca de los striders.
			Map.entry(BuiltInLootTables.NETHER_BRIDGE, List.of(Drop.single(0.10f, () -> ModItems.MAGMA_BOOTS.get()))),
			Map.entry(BuiltInLootTables.JUNGLE_TEMPLE, List.of(pressureCannon(0.10f),
					// Carcaj del bosque: entre las enredaderas del templo de la jungla.
					Drop.single(0.10f, () -> ModItems.FOREST_QUIVER.get()))),
			// Concha de nautilus: en tesoros enterrados y, menos, en naufragios.
			Map.entry(BuiltInLootTables.BURIED_TREASURE, List.of(Drop.single(0.15f, () -> ModItems.NAUTILUS_SHELL.get()))),
			// Anillo del berserker: en los bastiones, entre los brutos piglin.
			Map.entry(BuiltInLootTables.BASTION_OTHER, List.of(Drop.single(0.06f, () -> ModItems.BERSERKER_RING.get()))),
			Map.entry(BuiltInLootTables.BASTION_BRIDGE, List.of(Drop.single(0.06f, () -> ModItems.BERSERKER_RING.get()))),
			Map.entry(BuiltInLootTables.PILLAGER_OUTPOST, List.of(pressureCannon(0.08f),
					// Cuernos del carnero: los puestos de saqueadores ya guardan cuernos de cabra.
					Drop.single(0.10f, () -> ModItems.RAM_HORNS.get()))),
			Map.entry(BuiltInLootTables.SHIPWRECK_TREASURE, List.of(pressureCannon(0.06f),
					Drop.single(0.05f, () -> ModItems.NAUTILUS_SHELL.get()))),
			Map.entry(BuiltInLootTables.DESERT_PYRAMID, List.of(pressureCannon(0.10f),
					Drop.single(0.05f, () -> ModItems.CHARGED_CREEPER_HEART.get()),
					Drop.single(0.12f, () -> ModItems.LUCKY_RABBIT_FOOT.get()))),
			Map.entry(BuiltInLootTables.ABANDONED_CAMP_SECRET_CHEST, List.of(pressureCannon(0.12f),
					Drop.single(0.10f, () -> ModItems.LUCKY_RABBIT_FOOT.get()))),
			Map.entry(BuiltInLootTables.SHIPWRECK_SUPPLY, List.of(Drop.single(0.05f, () -> ModItems.LUCKY_RABBIT_FOOT.get()))),
			// Trompeta de cobre: en las cámaras de desafío, que son de cobre y están cuidadas (cobre casi nuevo).
			Map.entry(BuiltInLootTables.TRIAL_CHAMBERS_REWARD, List.of(trumpet(0.12f),
					// Guantelete eléctrico: cobre de las cámaras de desafío.
					Drop.single(0.06f, () -> ModItems.ELECTRIC_GAUNTLET.get()),
					// Breeze en una botella: donde vive el breeze.
					Drop.single(0.10f, () -> ModItems.BREEZE_BOTTLE.get()))),
			Map.entry(BuiltInLootTables.TRIAL_CHAMBERS_SUPPLY, List.of(trumpet(0.04f))),
			// Collar del murciélago: en la oscuridad de la ciudad antigua, junto a los fragmentos de eco.
			Map.entry(BuiltInLootTables.ANCIENT_CITY, List.of(Drop.single(0.10f, () -> ModItems.BAT_COLLAR.get()),
					// Collar del rey murciélago: coleccionable, muy raro.
					Drop.single(0.015f, () -> ModItems.KING_BAT_COLLAR.get()))),
			// Pata maldita: en el Nether, en tesoros de bastión y, rara vez, en portales en ruinas.
			Map.entry(BuiltInLootTables.BASTION_TREASURE, List.of(Drop.single(0.20f, () -> ModItems.CURSED_RABBIT_FOOT.get()))),
			Map.entry(BuiltInLootTables.RUINED_PORTAL, List.of(Drop.single(0.03f, () -> ModItems.CURSED_RABBIT_FOOT.get()))),
			// Anillo de la plaga: en la fortaleza, hogar de los silverfish.
			Map.entry(BuiltInLootTables.STRONGHOLD_LIBRARY, List.of(Drop.single(0.15f, () -> ModItems.PLAGUE_RING.get()))),
			Map.entry(BuiltInLootTables.STRONGHOLD_CORRIDOR, List.of(Drop.single(0.08f, () -> ModItems.PLAGUE_RING.get()))),
			Map.entry(BuiltInLootTables.STRONGHOLD_CROSSING, List.of(Drop.single(0.08f, () -> ModItems.PLAGUE_RING.get()))),
			// Anillo del festín: en el End, donde abundan los endermites. El par completo pide ir hasta allá.
			Map.entry(BuiltInLootTables.END_CITY_TREASURE, List.of(Drop.single(0.12f, () -> ModItems.FEAST_RING.get()))));

	/** Dónde aparece una reliquia: tabla de botín y probabilidad por cofre (o por muerte). */
	public record Source(ResourceKey<LootTable> table, float chance) {
		/** Clave de traducción del lugar, p. ej. loot.misfitrelics.chests.ancient_city. */
		public String translationKey() {
			return "loot.misfitrelics." + table.identifier().getPath().replace('/', '.');
		}
	}

	private ModLoot() {
	}

	/** Lugares por reliquia, calculados la primera vez que se piden (los ítems ya están registrados). */
	private static java.util.@org.jspecify.annotations.Nullable Map<Item, List<Source>> sourcesByItem;

	/** Todos los lugares donde se encuentra una reliquia (cualquiera de sus etapas de oxidación), de más a menos probable. */
	public static List<Source> sources(Item item) {
		if (sourcesByItem == null) {
			Map<Item, List<Source>> all = new java.util.IdentityHashMap<>();
			for (List<Drop> drops : DROPS.values()) {
				for (Drop drop : drops) {
					for (Item relic : drop.items().get().keySet()) {
						all.computeIfAbsent(relic, ModLoot::computeSources);
					}
				}
			}
			all.computeIfAbsent(ModItems.KING_BAT_COLLAR.get(), ModLoot::computeSources);
			sourcesByItem = all;
		}
		return sourcesByItem.getOrDefault(item, List.of());
	}

	private static List<Source> computeSources(Item item) {
		List<Source> found = new ArrayList<>();
		for (Map.Entry<ResourceKey<LootTable>, List<Drop>> entry : DROPS.entrySet()) {
			for (Drop drop : entry.getValue()) {
				if (drop.items().get().containsKey(item)) {
					found.add(new Source(entry.getKey(), drop.chance()));
				}
			}
		}
		if (item == ModItems.KING_BAT_COLLAR.get()) {
			EntityTypes.WARDEN.getDefaultLootTable().ifPresent(key -> found.add(new Source(key, WARDEN_KING_COLLAR_CHANCE)));
		}
		found.sort((a, b) -> Float.compare(b.chance(), a.chance()));
		return List.copyOf(found);
	}

	/** Línea discreta para el tooltip: "Se encuentra en: ciudad antigua, warden". */
	public static void appendWhere(Item item, java.util.function.Consumer<net.minecraft.network.chat.Component> tooltip) {
		List<Source> found = sources(item);
		if (found.isEmpty()) {
			return;
		}
		net.minecraft.network.chat.MutableComponent places = net.minecraft.network.chat.Component.empty();
		for (int i = 0; i < found.size(); i++) {
			if (i > 0) {
				places.append(", ");
			}
			places.append(net.minecraft.network.chat.Component.translatable(found.get(i).translationKey()));
		}
		tooltip.accept(net.minecraft.network.chat.Component.translatable("tooltip.misfitrelics.found_in", places)
				.withStyle(net.minecraft.ChatFormatting.DARK_GRAY, net.minecraft.ChatFormatting.ITALIC));
	}

	private static Drop pressureCannon(float chance) {
		return new Drop(chance, () -> Map.of(
				ModItems.cannonFor(CannonDesign.PRESSURE, CopperStage.COPPER), 1,
				ModItems.cannonFor(CannonDesign.PRESSURE, CopperStage.EXPOSED), 3,
				ModItems.cannonFor(CannonDesign.PRESSURE, CopperStage.WEATHERED), 4,
				ModItems.cannonFor(CannonDesign.PRESSURE, CopperStage.OXIDIZED), 2));
	}

	private static Drop trumpet(float chance) {
		return new Drop(chance, () -> Map.of(
				ModItems.trumpetFor(TrumpetStage.COPPER), 3,
				ModItems.trumpetFor(TrumpetStage.EXPOSED), 2,
				ModItems.trumpetFor(TrumpetStage.WEATHERED), 1));
	}

	/** El warden suelta el collar del rey murciélago (5 %) si lo mata un jugador. */
	private static final float WARDEN_KING_COLLAR_CHANCE = 0.05f;

	public static void init() {
		LootEvent.MODIFY_LOOT_TABLE.register((lookup, key, context, builtin) -> {
			if (builtin && EntityTypes.WARDEN.getDefaultLootTable().map(key::equals).orElse(false)) {
				context.addPool(LootPool.lootPool()
						.when(LootItemKilledByPlayerCondition.killedByPlayer())
						.when(LootItemRandomChanceCondition.randomChance(WARDEN_KING_COLLAR_CHANCE))
						.add(LootItem.lootTableItem(ModItems.KING_BAT_COLLAR.get())));
			}
			List<Drop> drops = DROPS.get(key);
			if (drops == null || !builtin) {
				return;
			}
			for (Drop drop : drops) {
				LootPool.Builder pool = LootPool.lootPool().when(LootItemRandomChanceCondition.randomChance(drop.chance()));
				List<Map.Entry<Item, Integer>> entries = new ArrayList<>(drop.items().get().entrySet());
				for (Map.Entry<Item, Integer> entry : entries) {
					pool.add(LootItem.lootTableItem(entry.getKey()).setWeight(entry.getValue()));
				}
				context.addPool(pool);
			}
		});
	}
}
