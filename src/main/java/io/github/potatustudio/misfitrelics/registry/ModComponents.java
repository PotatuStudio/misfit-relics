package io.github.potatustudio.misfitrelics.registry;

import com.mojang.serialization.Codec;
import io.github.potatustudio.misfitrelics.MisfitRelics;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;

public final class ModComponents {
	/** Collar del murciélago: ticks que faltan para que reviva el murciélago aliado (se muestra como barra). */
	public static final DataComponentType<Integer> BAT_REVIVE = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
			MisfitRelics.id("bat_revive"),
			new DataComponentType.Builder<Integer>().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());

	private ModComponents() {
	}

	public static void init() {
		// Fuerza la carga de la clase para registrar los componentes.
	}
}
