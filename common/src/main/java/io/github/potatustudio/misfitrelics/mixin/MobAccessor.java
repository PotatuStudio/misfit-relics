package io.github.potatustudio.misfitrelics.mixin;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Acceso a los objetivos de IA de un mob (para quitárselos a los bichos aliados de la plaga). */
@Mixin(Mob.class)
public interface MobAccessor {
	@Accessor("goalSelector")
	GoalSelector misfitrelics$goalSelector();

	@Accessor("targetSelector")
	GoalSelector misfitrelics$targetSelector();
}
