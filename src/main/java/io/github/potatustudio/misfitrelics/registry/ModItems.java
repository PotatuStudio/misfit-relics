package io.github.potatustudio.misfitrelics.registry;

import io.github.potatustudio.misfitrelics.MisfitRelics;
import io.github.potatustudio.misfitrelics.bat.BatCollarItem;
import io.github.potatustudio.misfitrelics.spud.SpudCannonItem;
import io.github.potatustudio.misfitrelics.trumpet.CopperTrumpetItem;
import io.github.potatustudio.misfitrelics.trumpet.TrumpetStage;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class ModItems {
	/** Pestaña "Combate" del inventario creativo (las constantes vanilla son privadas en 26.3). */
	private static final ResourceKey<CreativeModeTab> COMBAT_TAB =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("combat"));

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
		CreativeModeTabEvents.modifyOutputEvent(COMBAT_TAB).register(output -> {
			output.accept(SPUD_CANNON);
			output.accept(COPPER_TRUMPET);
			output.accept(EXPOSED_COPPER_TRUMPET);
			output.accept(WEATHERED_COPPER_TRUMPET);
			output.accept(OXIDIZED_COPPER_TRUMPET);
			output.accept(BAT_COLLAR);
		});
	}
}
