package io.github.potatustudio.misfitrelics.registry;

import io.github.potatustudio.misfitrelics.spud.CannonDesign;
import io.github.potatustudio.misfitrelics.util.CopperStage;
import java.util.Map;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;

/**
 * El cañón a presión (steampunk) solo se encuentra en cofres. Como lleva tiempo abandonado,
 * suele aparecer algo oxidado: pesa más el expuesto y el desgastado que el cobre nuevo.
 */
public final class ModLoot {
	/** Cofres donde puede aparecer y probabilidad por cofre. */
	private static final Map<ResourceKey<LootTable>, Float> PRESSURE_CANNON_CHESTS = Map.of(
			BuiltInLootTables.SIMPLE_DUNGEON, 0.08f,
			BuiltInLootTables.ABANDONED_MINESHAFT, 0.05f,
			BuiltInLootTables.DESERT_PYRAMID, 0.10f,
			BuiltInLootTables.JUNGLE_TEMPLE, 0.10f,
			BuiltInLootTables.PILLAGER_OUTPOST, 0.08f,
			BuiltInLootTables.SHIPWRECK_TREASURE, 0.06f,
			BuiltInLootTables.ABANDONED_CAMP_SECRET_CHEST, 0.12f);

	/** Peso de cada etapa de oxidación al aparecer en un cofre. */
	private static final Map<CopperStage, Integer> STAGE_WEIGHTS = Map.of(
			CopperStage.COPPER, 1,
			CopperStage.EXPOSED, 3,
			CopperStage.WEATHERED, 4,
			CopperStage.OXIDIZED, 2);

	private ModLoot() {
	}

	public static void init() {
		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			Float chance = PRESSURE_CANNON_CHESTS.get(key);
			if (chance == null || !source.isBuiltin()) {
				return;
			}
			LootPool.Builder pool = LootPool.lootPool().when(LootItemRandomChanceCondition.randomChance(chance));
			for (CopperStage stage : CopperStage.values()) {
				pool.add(LootItem.lootTableItem(ModItems.cannonFor(CannonDesign.PRESSURE, stage))
						.setWeight(STAGE_WEIGHTS.get(stage)));
			}
			tableBuilder.withPool(pool);
		});
	}
}
