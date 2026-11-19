package io.github.potatustudio.misfitrelics.neoforge;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import io.github.potatustudio.misfitrelics.MisfitRelics;
import io.github.potatustudio.misfitrelics.neoforge.client.MisfitRelicsNeoForgeClient;
import io.github.potatustudio.misfitrelics.registry.ModItems;
import io.github.potatustudio.misfitrelics.relic.AccessoryHooks;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import io.github.potatustudio.misfitrelics.relic.WornRelics;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

@Mod(MisfitRelics.MOD_ID)
public final class MisfitRelicsNeoForge {
	public MisfitRelicsNeoForge(IEventBus modBus) {
		MisfitRelics.declareEntityData();
		WornRelics.install(new CuriosWornRelics());
		EntityData.install(new NeoForgeEntityData(modBus));
		MisfitRelics.init();
		modBus.addListener(FMLCommonSetupEvent.class, event -> event.enqueueWork(MisfitRelicsNeoForge::registerCurios));
		if (FMLEnvironment.getDist() == Dist.CLIENT) {
			MisfitRelicsNeoForgeClient.init(modBus);
		}
	}

	/** Curios: ponerse, quitarse y atributos de las reliquias (AccessoryHooks), como TrinketCallback en Fabric. */
	private static void registerCurios() {
		for (Item item : ModItems.accessoryItems()) {
			AccessoryHooks hooks = (AccessoryHooks) item;
			CuriosApi.registerCurio(item, new ICurioItem() {
				@Override
				public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
					// Curios también avisa al cambiar componentes de la misma reliquia: solo cuenta si cambió el ítem.
					if (!prevStack.is(stack.getItem())) {
						hooks.onEquip(stack, slotContext.entity());
					}
				}

				@Override
				public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
					if (!newStack.is(stack.getItem())) {
						hooks.onUnequip(stack, slotContext.entity());
					}
				}

				@Override
				public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, Identifier id,
						ItemStack stack) {
					Multimap<Holder<Attribute>, AttributeModifier> modifiers = LinkedHashMultimap.create();
					hooks.forEachModifier(stack, slotContext.entity(), id, modifiers::put);
					return modifiers;
				}
			});
		}
	}
}
