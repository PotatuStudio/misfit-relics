package io.github.potatustudio.misfitrelics.registry;

import io.github.potatustudio.misfitrelics.MisfitRelics;
import io.github.potatustudio.misfitrelics.bat.BatCollarItem;
import io.github.potatustudio.misfitrelics.spud.SpudCannonItem;
import io.github.potatustudio.misfitrelics.trumpet.CopperTrumpetItem;
import io.github.potatustudio.misfitrelics.trumpet.TrumpetStage;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

public final class ModItems {
	/** Pestaña propia del mod en el inventario creativo. */
	public static final ResourceKey<CreativeModeTab> CREATIVE_TAB_KEY =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, MisfitRelics.id("main"));

	public static final Item SPUD_CANNON = register("spud_cannon", SpudCannonItem::new,
			new Item.Properties()
					.stacksTo(1)
					.durability(SpudCannonItem.DURABILITY)
					.repairable(Items.IRON_INGOT)
					.enchantable(1));

	private static final Map<TrumpetStage, Item> TRUMPETS = new EnumMap<>(TrumpetStage.class);
	public static final Item COPPER_TRUMPET = registerTrumpet("copper_trumpet", TrumpetStage.COPPER);
	public static final Item EXPOSED_COPPER_TRUMPET = registerTrumpet("exposed_copper_trumpet", TrumpetStage.EXPOSED);
	public static final Item WEATHERED_COPPER_TRUMPET = registerTrumpet("weathered_copper_trumpet", TrumpetStage.WEATHERED);
	public static final Item OXIDIZED_COPPER_TRUMPET = registerTrumpet("oxidized_copper_trumpet", TrumpetStage.OXIDIZED);

	public static final Item BAT_COLLAR = register("bat_collar", BatCollarItem::new,
			new Item.Properties().stacksTo(1));

	private ModItems() {
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
						.repairable(Items.COPPER_INGOT));
		TRUMPETS.put(stage, item);
		return item;
	}

	private static Item register(String path, Function<Item.Properties, Item> factory, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, MisfitRelics.id(path));
		return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
	}

	public static void init() {
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CREATIVE_TAB_KEY, FabricCreativeModeTab.builder()
				.title(Component.translatable("itemGroup.misfitrelics"))
				.icon(() -> new ItemStack(SPUD_CANNON))
				.displayItems((parameters, output) -> {
					output.accept(SPUD_CANNON);
					output.accept(COPPER_TRUMPET);
					output.accept(EXPOSED_COPPER_TRUMPET);
					output.accept(WEATHERED_COPPER_TRUMPET);
					output.accept(OXIDIZED_COPPER_TRUMPET);
					output.accept(BAT_COLLAR);
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
