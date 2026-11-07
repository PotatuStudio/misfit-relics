package io.github.potatustudio.misfitrelics.registry;

import io.github.potatustudio.misfitrelics.MisfitRelics;
import io.github.potatustudio.misfitrelics.spud.SpudCannonItem;
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

	private ModItems() {
	}

	private static Item register(String path, Function<Item.Properties, Item> factory, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, MisfitRelics.id(path));
		return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
	}

	public static void init() {
		CreativeModeTabEvents.modifyOutputEvent(COMBAT_TAB).register(output -> output.accept(SPUD_CANNON));
	}
}
