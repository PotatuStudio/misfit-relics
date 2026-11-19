package io.github.potatustudio.misfitrelics.registry;

import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.potatustudio.misfitrelics.MisfitRelics;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;

public final class ModComponents {
	private static final DeferredRegister<DataComponentType<?>> COMPONENTS =
			DeferredRegister.create(MisfitRelics.MOD_ID, Registries.DATA_COMPONENT_TYPE);

	/** Collar del murciélago: ticks que faltan para que reviva el murciélago aliado (se muestra como barra). */
	public static final RegistrySupplier<DataComponentType<Integer>> BAT_REVIVE = COMPONENTS.register("bat_revive",
			() -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());

	private ModComponents() {
	}

	public static void init() {
		COMPONENTS.register();
	}
}
