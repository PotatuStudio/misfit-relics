package io.github.potatustudio.misfitrelics.relic;

import java.util.function.BiConsumer;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

/**
 * Lo que hace una reliquia al ponérsela, al quitársela y mientras la lleva puesta (atributos). Cada loader lo conecta
 * con su biblioteca de accesorios a todos los ítems del mod que implementen esta interfaz: {@code TrinketCallback} en
 * Fabric e {@code ICurioItem} en NeoForge.
 */
public interface AccessoryHooks {
	/** Al ponérsela (sonido y partículas para que se note). */
	default void onEquip(ItemStack stack, LivingEntity entity) {
	}

	default void onUnequip(ItemStack stack, LivingEntity entity) {
	}

	/** Atributos mientras se lleva puesta; la biblioteca los aplica y los quita. {@code id} es único por espacio. */
	default void forEachModifier(ItemStack stack, LivingEntity entity, Identifier id,
			BiConsumer<Holder<Attribute>, AttributeModifier> consumer) {
	}
}
