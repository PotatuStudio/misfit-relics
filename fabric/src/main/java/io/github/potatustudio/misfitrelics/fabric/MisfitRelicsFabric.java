package io.github.potatustudio.misfitrelics.fabric;

import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.callback.TrinketCallback;
import io.github.potatustudio.misfitrelics.MisfitRelics;
import io.github.potatustudio.misfitrelics.registry.ModItems;
import io.github.potatustudio.misfitrelics.relic.AccessoryHooks;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import io.github.potatustudio.misfitrelics.relic.WornRelics;
import java.util.function.BiConsumer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class MisfitRelicsFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		MisfitRelics.declareEntityData();
		WornRelics.install(new TrinketsWornRelics());
		EntityData.install(new FabricEntityData());
		MisfitRelics.init();
		// Trinkets: ponerse, quitarse y atributos de las reliquias (AccessoryHooks).
		for (Item item : ModItems.accessoryItems()) {
			AccessoryHooks hooks = (AccessoryHooks) item;
			TrinketCallback.setCallback(item, new TrinketCallback() {
				@Override
				public void onEquip(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
					hooks.onEquip(stack, entity);
				}

				@Override
				public void onUnequip(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
					hooks.onUnequip(stack, entity);
				}

				@Override
				public void forEachTrinketModifier(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity, Identifier id,
						BiConsumer<Holder<Attribute>, AttributeModifier> consumer) {
					hooks.forEachModifier(stack, entity, id, consumer);
				}
			});
		}
	}
}
