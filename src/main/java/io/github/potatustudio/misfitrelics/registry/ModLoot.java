package io.github.potatustudio.misfitrelics.registry;

import io.github.potatustudio.misfitrelics.spud.CannonDesign;
import io.github.potatustudio.misfitrelics.trumpet.TrumpetStage;
import io.github.potatustudio.misfitrelics.util.CopperStage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
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
			Map.entry(BuiltInLootTables.SIMPLE_DUNGEON, List.of(pressureCannon(0.08f))),
			Map.entry(BuiltInLootTables.ABANDONED_MINESHAFT, List.of(pressureCannon(0.05f))),
			Map.entry(BuiltInLootTables.JUNGLE_TEMPLE, List.of(pressureCannon(0.10f))),
			Map.entry(BuiltInLootTables.PILLAGER_OUTPOST, List.of(pressureCannon(0.08f))),
			Map.entry(BuiltInLootTables.SHIPWRECK_TREASURE, List.of(pressureCannon(0.06f))),
			Map.entry(BuiltInLootTables.DESERT_PYRAMID, List.of(pressureCannon(0.10f),
					Drop.single(0.12f, () -> ModItems.LUCKY_RABBIT_FOOT))),
			Map.entry(BuiltInLootTables.ABANDONED_CAMP_SECRET_CHEST, List.of(pressureCannon(0.12f),
					Drop.single(0.10f, () -> ModItems.LUCKY_RABBIT_FOOT))),
			Map.entry(BuiltInLootTables.SHIPWRECK_SUPPLY, List.of(Drop.single(0.05f, () -> ModItems.LUCKY_RABBIT_FOOT))),
			// Trompeta de cobre: en las cámaras de desafío, que son de cobre y están cuidadas (cobre casi nuevo).
			Map.entry(BuiltInLootTables.TRIAL_CHAMBERS_REWARD, List.of(trumpet(0.12f))),
			Map.entry(BuiltInLootTables.TRIAL_CHAMBERS_SUPPLY, List.of(trumpet(0.04f))),
			// Collar del murciélago: en la oscuridad de la ciudad antigua, junto a los fragmentos de eco.
			Map.entry(BuiltInLootTables.ANCIENT_CITY, List.of(Drop.single(0.10f, () -> ModItems.BAT_COLLAR))),
			// Pata maldita: en el Nether, en tesoros de bastión y, rara vez, en portales en ruinas.
			Map.entry(BuiltInLootTables.BASTION_TREASURE, List.of(Drop.single(0.20f, () -> ModItems.CURSED_RABBIT_FOOT))),
			Map.entry(BuiltInLootTables.RUINED_PORTAL, List.of(Drop.single(0.03f, () -> ModItems.CURSED_RABBIT_FOOT))),
			// Anillo de la plaga: en la fortaleza, hogar de los silverfish.
			Map.entry(BuiltInLootTables.STRONGHOLD_LIBRARY, List.of(Drop.single(0.15f, () -> ModItems.PLAGUE_RING))),
			Map.entry(BuiltInLootTables.STRONGHOLD_CORRIDOR, List.of(Drop.single(0.08f, () -> ModItems.PLAGUE_RING))),
			Map.entry(BuiltInLootTables.STRONGHOLD_CROSSING, List.of(Drop.single(0.08f, () -> ModItems.PLAGUE_RING))),
			// Anillo del festín: en el End, donde abundan los endermites. El par completo pide ir hasta allá.
			Map.entry(BuiltInLootTables.END_CITY_TREASURE, List.of(Drop.single(0.12f, () -> ModItems.FEAST_RING))));

	private ModLoot() {
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

	public static void init() {
		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			List<Drop> drops = DROPS.get(key);
			if (drops == null || !source.isBuiltin()) {
				return;
			}
			for (Drop drop : drops) {
				LootPool.Builder pool = LootPool.lootPool().when(LootItemRandomChanceCondition.randomChance(drop.chance()));
				List<Map.Entry<Item, Integer>> entries = new ArrayList<>(drop.items().get().entrySet());
				for (Map.Entry<Item, Integer> entry : entries) {
					pool.add(LootItem.lootTableItem(entry.getKey()).setWeight(entry.getValue()));
				}
				tableBuilder.withPool(pool);
			}
		});
	}
}
