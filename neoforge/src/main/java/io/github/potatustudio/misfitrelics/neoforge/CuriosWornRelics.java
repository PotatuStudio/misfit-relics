package io.github.potatustudio.misfitrelics.neoforge;

import io.github.potatustudio.misfitrelics.relic.WornRelics;
import java.util.Optional;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Accesorios en NeoForge: Curios. Los espacios se asignan con data/curios/tags/item y data/misfitrelics/curios/entities.
 * Curios sincroniza solo los cambios de componentes de la pila puesta, así que avisar del cambio no hace nada.
 */
public final class CuriosWornRelics implements WornRelics.Provider {
	@Override
	public Optional<WornRelics.Worn> find(LivingEntity entity, Item item) {
		return CuriosApi.getCuriosInventory(entity)
				.flatMap(inventory -> inventory.findFirstCurio(item))
				.map(result -> new WornRelics.Worn(result.stack(), () -> {
				}));
	}
}
