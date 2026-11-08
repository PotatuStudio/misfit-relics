package io.github.potatustudio.misfitrelics.registry;

import eu.pb4.trinkets.api.callback.TrinketCallback;
import io.github.potatustudio.misfitrelics.MisfitRelics;
import io.github.potatustudio.misfitrelics.bat.BatCollar;
import io.github.potatustudio.misfitrelics.bat.BatCollarItem;
import io.github.potatustudio.misfitrelics.combat.CombatItem;
import io.github.potatustudio.misfitrelics.luck.LuckyRabbitFootItem;
import io.github.potatustudio.misfitrelics.mobility.Mobility;
import io.github.potatustudio.misfitrelics.mobility.MobilityItem;
import io.github.potatustudio.misfitrelics.plague.BugRingItem;
import io.github.potatustudio.misfitrelics.spud.CannonDesign;
import io.github.potatustudio.misfitrelics.spud.SpudCannonItem;
import io.github.potatustudio.misfitrelics.trumpet.CopperTrumpetItem;
import io.github.potatustudio.misfitrelics.trumpet.TrumpetStage;
import io.github.potatustudio.misfitrelics.util.CopperStage;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.item.component.Weapon;

public final class ModItems {
	/** Pestaña propia del mod en el inventario creativo. */
	public static final ResourceKey<CreativeModeTab> CREATIVE_TAB_KEY =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, MisfitRelics.id("main"));

	/** Cañones de papas: 2 diseños × 4 etapas de oxidación. */
	private static final Map<CannonDesign, Map<CopperStage, Item>> CANNONS = new EnumMap<>(CannonDesign.class);

	static {
		for (CannonDesign design : CannonDesign.values()) {
			Map<CopperStage, Item> byStage = new EnumMap<>(CopperStage.class);
			for (CopperStage stage : CopperStage.values()) {
				byStage.put(stage, register(design.id(stage), properties -> new SpudCannonItem(design, stage, properties),
						new Item.Properties()
								.stacksTo(1)
								.durability(design.durability)
								.repairable(Items.COPPER_INGOT)
								.enchantable(1)));
			}
			CANNONS.put(design, byStage);
		}
	}

	/** Cañón rústico nuevo (el que se craftea). */
	public static final Item SPUD_CANNON = cannonFor(CannonDesign.RUSTIC, CopperStage.COPPER);

	private static final Map<TrumpetStage, Item> TRUMPETS = new EnumMap<>(TrumpetStage.class);
	public static final Item COPPER_TRUMPET = registerTrumpet("copper_trumpet", TrumpetStage.COPPER);
	public static final Item EXPOSED_COPPER_TRUMPET = registerTrumpet("exposed_copper_trumpet", TrumpetStage.EXPOSED);
	public static final Item WEATHERED_COPPER_TRUMPET = registerTrumpet("weathered_copper_trumpet", TrumpetStage.WEATHERED);
	public static final Item OXIDIZED_COPPER_TRUMPET = registerTrumpet("oxidized_copper_trumpet", TrumpetStage.OXIDIZED);

	public static final Item BAT_COLLAR = register("bat_collar",
			properties -> new BatCollarItem(false, properties), new Item.Properties().stacksTo(1)
					.component(DataComponents.USE_COOLDOWN, new UseCooldown(0.05f, Optional.of(BatCollar.COOLDOWN_GROUP))));
	public static final Item KING_BAT_COLLAR = register("king_bat_collar",
			properties -> new BatCollarItem(true, properties), new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)
					.component(DataComponents.USE_COOLDOWN, new UseCooldown(0.05f, Optional.of(BatCollar.COOLDOWN_GROUP))));

	public static final Item LUCKY_RABBIT_FOOT = register("lucky_rabbit_foot",
			properties -> new LuckyRabbitFootItem(false, properties), new Item.Properties().stacksTo(1));
	public static final Item CURSED_RABBIT_FOOT = register("cursed_rabbit_foot",
			properties -> new LuckyRabbitFootItem(true, properties), new Item.Properties().stacksTo(1));

	public static final Item PLAGUE_RING = register("plague_ring",
			properties -> new BugRingItem(false, properties), new Item.Properties().stacksTo(1));
	public static final Item FEAST_RING = register("feast_ring",
			properties -> new BugRingItem(true, properties), new Item.Properties().stacksTo(1));

	public static final Item SILK_GLOVES = registerMobility("silk_gloves", MobilityItem.Kind.SILK_GLOVES, Rarity.UNCOMMON);
	public static final Item WIDOW_GLOVES = registerMobility("widow_gloves", MobilityItem.Kind.WIDOW_GLOVES, Rarity.RARE);
	public static final Item BREEZE_BOTTLE = registerMobility("breeze_bottle", MobilityItem.Kind.BREEZE_BOTTLE, Rarity.UNCOMMON);
	public static final Item MAGMA_BOOTS = registerMobility("magma_boots", MobilityItem.Kind.MAGMA_BOOTS, Rarity.UNCOMMON);
	public static final Item RAM_HORNS = register("ram_horns",
			properties -> new MobilityItem(MobilityItem.Kind.RAM_HORNS, properties), new Item.Properties().stacksTo(1)
					.rarity(Rarity.UNCOMMON)
					.component(DataComponents.USE_COOLDOWN, new UseCooldown(0.05f, Optional.of(Mobility.RAM_COOLDOWN_GROUP))));

	private ModItems() {
	}

	/** Cañón de un diseño en una etapa de oxidación. */
	public static Item cannonFor(CannonDesign design, CopperStage stage) {
		return CANNONS.get(design).get(stage);
	}

	/** Trompeta correspondiente a una etapa de oxidación. */
	public static Item trumpetFor(TrumpetStage stage) {
		return TRUMPETS.get(stage);
	}

	private static Item registerTrumpet(String path, TrumpetStage stage) {
		Item item = register(path, properties -> new CopperTrumpetItem(stage, properties),
				new Item.Properties()
						.stacksTo(1)
						.durability(CopperTrumpetItem.DURABILITY)
						.repairable(Items.COPPER_INGOT)
						.attributes(CopperTrumpetItem.attributes())
						.component(DataComponents.WEAPON, new Weapon(1))
						.enchantable(13));
		TRUMPETS.put(stage, item);
		return item;
	}

	public static final Item ELECTRIC_GAUNTLET = register("electric_gauntlet",
			properties -> new CombatItem(CombatItem.Kind.ELECTRIC_GAUNTLET, properties), new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
	public static final Item CHARGED_CREEPER_HEART = register("charged_creeper_heart",
			properties -> new CombatItem(CombatItem.Kind.CHARGED_CREEPER_HEART, properties), new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
	public static final Item FOREST_QUIVER = register("forest_quiver",
			properties -> new CombatItem(CombatItem.Kind.FOREST_QUIVER, properties), new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
	public static final Item WATCHER_QUIVER = register("watcher_quiver",
			properties -> new CombatItem(CombatItem.Kind.WATCHER_QUIVER, properties), new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
	public static final Item NAUTILUS_SHELL = register("nautilus_shell",
			properties -> new CombatItem(CombatItem.Kind.NAUTILUS_SHELL, properties), new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
	public static final Item BERSERKER_RING = register("berserker_ring",
			properties -> new CombatItem(CombatItem.Kind.BERSERKER_RING, properties), new Item.Properties().stacksTo(1).rarity(Rarity.RARE));

	private static Item registerMobility(String path, MobilityItem.Kind kind, Rarity rarity) {
		return register(path, properties -> new MobilityItem(kind, properties), new Item.Properties().stacksTo(1).rarity(rarity));
	}

	private static Item register(String path, Function<Item.Properties, Item> factory, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, MisfitRelics.id(path));
		return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
	}

	public static void init() {
		// Trinkets: reacción al ponerse el collar (BatCollarItem#onEquip).
		TrinketCallback.setCallback(BAT_COLLAR, (TrinketCallback) BAT_COLLAR);
		TrinketCallback.setCallback(KING_BAT_COLLAR, (TrinketCallback) KING_BAT_COLLAR);
		TrinketCallback.setCallback(LUCKY_RABBIT_FOOT, (TrinketCallback) LUCKY_RABBIT_FOOT);
		TrinketCallback.setCallback(CURSED_RABBIT_FOOT, (TrinketCallback) CURSED_RABBIT_FOOT);
		TrinketCallback.setCallback(PLAGUE_RING, (TrinketCallback) PLAGUE_RING);
		TrinketCallback.setCallback(FEAST_RING, (TrinketCallback) FEAST_RING);
		TrinketCallback.setCallback(SILK_GLOVES, (TrinketCallback) SILK_GLOVES);
		TrinketCallback.setCallback(WIDOW_GLOVES, (TrinketCallback) WIDOW_GLOVES);
		TrinketCallback.setCallback(BREEZE_BOTTLE, (TrinketCallback) BREEZE_BOTTLE);
		TrinketCallback.setCallback(MAGMA_BOOTS, (TrinketCallback) MAGMA_BOOTS);
		TrinketCallback.setCallback(RAM_HORNS, (TrinketCallback) RAM_HORNS);
		TrinketCallback.setCallback(ELECTRIC_GAUNTLET, (TrinketCallback) ELECTRIC_GAUNTLET);
		TrinketCallback.setCallback(CHARGED_CREEPER_HEART, (TrinketCallback) CHARGED_CREEPER_HEART);
		TrinketCallback.setCallback(FOREST_QUIVER, (TrinketCallback) FOREST_QUIVER);
		TrinketCallback.setCallback(WATCHER_QUIVER, (TrinketCallback) WATCHER_QUIVER);
		TrinketCallback.setCallback(NAUTILUS_SHELL, (TrinketCallback) NAUTILUS_SHELL);
		TrinketCallback.setCallback(BERSERKER_RING, (TrinketCallback) BERSERKER_RING);
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CREATIVE_TAB_KEY, FabricCreativeModeTab.builder()
				.title(Component.translatable("itemGroup.misfitrelics"))
				.icon(() -> new ItemStack(SPUD_CANNON))
				.displayItems((parameters, output) -> {
					for (CannonDesign design : CannonDesign.values()) {
						for (CopperStage stage : CopperStage.values()) {
							output.accept(cannonFor(design, stage));
						}
					}
					output.accept(COPPER_TRUMPET);
					output.accept(EXPOSED_COPPER_TRUMPET);
					output.accept(WEATHERED_COPPER_TRUMPET);
					output.accept(OXIDIZED_COPPER_TRUMPET);
					output.accept(BAT_COLLAR);
					output.accept(KING_BAT_COLLAR);
					output.accept(LUCKY_RABBIT_FOOT);
					output.accept(CURSED_RABBIT_FOOT);
					output.accept(PLAGUE_RING);
					output.accept(FEAST_RING);
					output.accept(SILK_GLOVES);
					output.accept(WIDOW_GLOVES);
					output.accept(BREEZE_BOTTLE);
					output.accept(MAGMA_BOOTS);
					output.accept(RAM_HORNS);
					output.accept(ELECTRIC_GAUNTLET);
					output.accept(CHARGED_CREEPER_HEART);
					output.accept(FOREST_QUIVER);
					output.accept(WATCHER_QUIVER);
					output.accept(NAUTILUS_SHELL);
					output.accept(BERSERKER_RING);
					// Pociones y flechas de hedor en todas sus variantes.
					for (Holder<Potion> potion : List.of(ModPotions.STENCH, ModPotions.LONG_STENCH, ModPotions.STRONG_STENCH)) {
						for (Item container : List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION, Items.TIPPED_ARROW)) {
							output.accept(PotionContents.createItemStack(container, potion));
						}
					}
				})
				.build());
	}
}
