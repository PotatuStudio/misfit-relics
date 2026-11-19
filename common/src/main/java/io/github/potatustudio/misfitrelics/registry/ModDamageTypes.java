package io.github.potatustudio.misfitrelics.registry;

import io.github.potatustudio.misfitrelics.MisfitRelics;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

/** Tipos de daño definidos en data/misfitrelics/damage_type/. */
public final class ModDamageTypes {
	public static final ResourceKey<DamageType> STENCH = ResourceKey.create(Registries.DAMAGE_TYPE, MisfitRelics.id("stench"));

	/** Electrocución del guantelete eléctrico y del corazón de creeper cargado. */
	public static final ResourceKey<DamageType> ELECTROCUTION =
			ResourceKey.create(Registries.DAMAGE_TYPE, MisfitRelics.id("electrocution"));

	private ModDamageTypes() {
	}
}
