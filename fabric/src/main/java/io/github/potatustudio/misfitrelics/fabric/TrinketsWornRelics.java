package io.github.potatustudio.misfitrelics.fabric;

import eu.pb4.trinkets.api.TrinketsApi;
import io.github.potatustudio.misfitrelics.relic.WornRelics;
import java.util.Optional;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

/** Accesorios en Fabric: Trinkets Updated. Los espacios se asignan en data/trinkets y el render en assets/.../trinkets. */
public final class TrinketsWornRelics implements WornRelics.Provider {
	@Override
	public Optional<WornRelics.Worn> find(LivingEntity entity, Item item) {
		return TrinketsApi.getAttachment(entity).findFirst(item)
				.filter(access -> !access.get().isEmpty())
				.map(access -> new WornRelics.Worn(access.get(), () -> access.inventory().setChanged()));
	}
}
