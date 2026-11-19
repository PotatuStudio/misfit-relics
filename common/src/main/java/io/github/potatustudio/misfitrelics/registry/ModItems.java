package io.github.potatustudio.misfitrelics.registry;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.potatustudio.misfitrelics.MisfitRelics;
import io.github.potatustudio.misfitrelics.bat.BatCollar;
import io.github.potatustudio.misfitrelics.bat.BatCollarItem;
import io.github.potatustudio.misfitrelics.combat.CombatItem;
import io.github.potatustudio.misfitrelics.luck.LuckyRabbitFootItem;
import io.github.potatustudio.misfitrelics.mobility.Mobility;
import io.github.potatustudio.misfitrelics.mobility.MobilityItem;
import io.github.potatustudio.misfitrelics.relic.AccessoryHooks;
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
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
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
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MisfitRelics.MOD_ID, Registries.ITEM);
	private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(MisfitRelics.MOD_ID, Registries.CREATIVE_MODE_TAB);

	/** Cañones de papas: 2 diseños × 4 etapas de oxidación. */
	private static final Map<CannonDesign, Map<CopperStage, RegistrySupplier<Item>>> CANNONS = new EnumMap<>(CannonDesign.class);

	static {
		for (CannonDesign design : CannonDesign.values()) {
			Map<CopperStage, RegistrySupplier<Item>> byStage = new EnumMap<>(CopperStage.class);
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
	public static final RegistrySupplier<Item> SPUD_CANNON = CANNONS.get(CannonDesign.RUSTIC).get(CopperStage.COPPER);

	private static final Map<TrumpetStage, RegistrySupplier<Item>> TRUMPETS = new EnumMap<>(TrumpetStage.class);
	public static final RegistrySupplier<Item> COPPER_TRUMPET = registerTrumpet("copper_trumpet", TrumpetStage.COPPER);
	public static final RegistrySupplier<Item> EXPOSED_COPPER_TRUMPET = registerTrumpet("exposed_copper_trumpet", TrumpetStage.EXPOSED);
	public static final RegistrySupplier<Item> WEATHERED_COPPER_TRUMPET = registerTrumpet("weathered_copper_trumpet", TrumpetStage.WEATHERED);
	public static final RegistrySupplier<Item> OXIDIZED_COPPER_TRUMPET = registerTrumpet("oxidized_copper_trumpet", TrumpetStage.OXIDIZED);

	/** Solo para el ícono de la pestaña creativa (el amuleto del ícono del mod); no aparece en el inventario. */
	public static final RegistrySupplier<Item> RELIC_ICON = register("relic_icon", Item::new, new Item.Properties());

	public static final RegistrySupplier<Item> BAT_COLLAR = register("bat_collar",
			properties -> new BatCollarItem(false, properties), new Item.Properties().stacksTo(1)
					.component(DataComponents.USE_COOLDOWN, new UseCooldown(0.05f, Optional.of(BatCollar.COOLDOWN_GROUP))));
	public static final RegistrySupplier<Item> KING_BAT_COLLAR = register("king_bat_collar",
			properties -> new BatCollarItem(true, properties), new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)
					.component(DataComponents.USE_COOLDOWN, new UseCooldown(0.05f, Optional.of(BatCollar.COOLDOWN_GROUP))));

	public static final RegistrySupplier<Item> LUCKY_RABBIT_FOOT = register("lucky_rabbit_foot",
			properties -> new LuckyRabbitFootItem(false, properties), new Item.Properties().stacksTo(1));
	public static final RegistrySupplier<Item> CURSED_RABBIT_FOOT = register("cursed_rabbit_foot",
			properties -> new LuckyRabbitFootItem(true, properties), new Item.Properties().stacksTo(1));

	public static final RegistrySupplier<Item> PLAGUE_RING = register("plague_ring",
			properties -> new BugRingItem(false, properties), new Item.Properties().stacksTo(1));
	public static final RegistrySupplier<Item> FEAST_RING = register("feast_ring",
			properties -> new BugRingItem(true, properties), new Item.Properties().stacksTo(1));

	public static final RegistrySupplier<Item> SILK_GLOVES = registerMobility("silk_gloves", MobilityItem.Kind.SILK_GLOVES, Rarity.UNCOMMON);
	public static final RegistrySupplier<Item> WIDOW_GLOVES = registerMobility("widow_gloves", MobilityItem.Kind.WIDOW_GLOVES, Rarity.RARE);
	public static final RegistrySupplier<Item> BREEZE_BOTTLE = registerMobility("breeze_bottle", MobilityItem.Kind.BREEZE_BOTTLE, Rarity.UNCOMMON);
	public static final RegistrySupplier<Item> MAGMA_BOOTS = registerMobility("magma_boots", MobilityItem.Kind.MAGMA_BOOTS, Rarity.UNCOMMON);
	public static final RegistrySupplier<Item> RAM_HORNS = register("ram_horns",
			properties -> new MobilityItem(MobilityItem.Kind.RAM_HORNS, properties), new Item.Properties().stacksTo(1)
					.rarity(Rarity.UNCOMMON)
					.component(DataComponents.USE_COOLDOWN, new UseCooldown(0.05f, Optional.of(Mobility.RAM_COOLDOWN_GROUP))));

	private ModItems() {
	}

	/** Cañón de un diseño en una etapa de oxidación. */
	public static Item cannonFor(CannonDesign design, CopperStage stage) {
		return CANNONS.get(design).get(stage).get();
	}

	/** Trompeta correspondiente a una etapa de oxidación. */
	public static Item trumpetFor(TrumpetStage stage) {
		return TRUMPETS.get(stage).get();
	}

	private static RegistrySupplier<Item> registerTrumpet(String path, TrumpetStage stage) {
		RegistrySupplier<Item> item = register(path, properties -> new CopperTrumpetItem(stage, properties),
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

	public static final RegistrySupplier<Item> ELECTRIC_GAUNTLET = register("electric_gauntlet",
			properties -> new CombatItem(CombatItem.Kind.ELECTRIC_GAUNTLET, properties), new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
	public static final RegistrySupplier<Item> CHARGED_CREEPER_HEART = register("charged_creeper_heart",
			properties -> new CombatItem(CombatItem.Kind.CHARGED_CREEPER_HEART, properties), new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
	public static final RegistrySupplier<Item> FOREST_QUIVER = register("forest_quiver",
			properties -> new CombatItem(CombatItem.Kind.FOREST_QUIVER, properties), new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
	public static final RegistrySupplier<Item> WATCHER_QUIVER = register("watcher_quiver",
			properties -> new CombatItem(CombatItem.Kind.WATCHER_QUIVER, properties), new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
	public static final RegistrySupplier<Item> NAUTILUS_SHELL = register("nautilus_shell",
			properties -> new CombatItem(CombatItem.Kind.NAUTILUS_SHELL, properties), new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
	public static final RegistrySupplier<Item> BERSERKER_RING = register("berserker_ring",
			properties -> new CombatItem(CombatItem.Kind.BERSERKER_RING, properties), new Item.Properties().stacksTo(1).rarity(Rarity.RARE));

	private static RegistrySupplier<Item> registerMobility(String path, MobilityItem.Kind kind, Rarity rarity) {
		return register(path, properties -> new MobilityItem(kind, properties), new Item.Properties().stacksTo(1).rarity(rarity));
	}

	private static RegistrySupplier<Item> register(String path, Function<Item.Properties, Item> factory, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, MisfitRelics.id(path));
		return ITEMS.register(path, () -> factory.apply(properties.setId(key)));
	}

	/** Pestaña propia del mod en el inventario creativo. */
	public static final RegistrySupplier<CreativeModeTab> TAB = TABS.register("main", () -> CreativeTabRegistry.create(builder -> builder
			.title(Component.translatable("itemGroup.misfitrelics"))
			.icon(() -> new ItemStack(RELIC_ICON.get()))
			.displayItems((parameters, output) -> {
				for (CannonDesign design : CannonDesign.values()) {
					for (CopperStage stage : CopperStage.values()) {
						output.accept(cannonFor(design, stage));
					}
				}
				output.accept(COPPER_TRUMPET.get());
				output.accept(EXPOSED_COPPER_TRUMPET.get());
				output.accept(WEATHERED_COPPER_TRUMPET.get());
				output.accept(OXIDIZED_COPPER_TRUMPET.get());
				output.accept(BAT_COLLAR.get());
				output.accept(KING_BAT_COLLAR.get());
				output.accept(LUCKY_RABBIT_FOOT.get());
				output.accept(CURSED_RABBIT_FOOT.get());
				output.accept(PLAGUE_RING.get());
				output.accept(FEAST_RING.get());
				output.accept(SILK_GLOVES.get());
				output.accept(WIDOW_GLOVES.get());
				output.accept(BREEZE_BOTTLE.get());
				output.accept(MAGMA_BOOTS.get());
				output.accept(RAM_HORNS.get());
				output.accept(ELECTRIC_GAUNTLET.get());
				output.accept(CHARGED_CREEPER_HEART.get());
				output.accept(FOREST_QUIVER.get());
				output.accept(WATCHER_QUIVER.get());
				output.accept(NAUTILUS_SHELL.get());
				output.accept(BERSERKER_RING.get());
				// Pociones y flechas de hedor en todas sus variantes.
				for (Holder<Potion> potion : List.of(ModPotions.STENCH.asHolder(), ModPotions.LONG_STENCH.asHolder(), ModPotions.STRONG_STENCH.asHolder())) {
					for (Item container : List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION, Items.TIPPED_ARROW)) {
						output.accept(PotionContents.createItemStack(container, potion));
					}
				}
			})));

	/** Las reliquias que tienen ganchos de accesorio (ver {@link AccessoryHooks}); cada loader las conecta con su biblioteca. */
	public static List<Item> accessoryItems() {
		List<Item> items = new java.util.ArrayList<>();
		for (RegistrySupplier<Item> item : ITEMS) {
			if (item.get() instanceof AccessoryHooks) {
				items.add(item.get());
			}
		}
		return items;
	}

	public static void init() {
		ITEMS.register();
		TABS.register();
	}
}
