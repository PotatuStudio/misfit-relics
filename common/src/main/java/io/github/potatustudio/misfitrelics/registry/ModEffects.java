package io.github.potatustudio.misfitrelics.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.potatustudio.misfitrelics.MisfitRelics;
import io.github.potatustudio.misfitrelics.combat.EntangledEffect;
import io.github.potatustudio.misfitrelics.stench.StenchEffect;
import io.github.potatustudio.misfitrelics.trumpet.StunnedEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;

/** Efectos del mod. Para un {@code MobEffectInstance} o {@code hasEffect} se usa el holder: {@code STENCH.asHolder()}. */
public final class ModEffects {
	private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(MisfitRelics.MOD_ID, Registries.MOB_EFFECT);

	public static final RegistrySupplier<MobEffect> STENCH = EFFECTS.register("stench", StenchEffect::new);
	public static final RegistrySupplier<MobEffect> STUNNED = EFFECTS.register("stunned", StunnedEffect::new);
	public static final RegistrySupplier<MobEffect> ENTANGLED = EFFECTS.register("entangled", EntangledEffect::new);

	private ModEffects() {
	}

	public static void init() {
		EFFECTS.register();
	}
}
