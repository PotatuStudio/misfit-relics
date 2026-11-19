package io.github.potatustudio.misfitrelics.mixin;

import io.github.potatustudio.misfitrelics.mobility.Mobility;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Botas de magma sobre lava que corre: vanilla solo deja pararse sobre bloques fuente (la lava que corre no tiene
 * colisión para nadie). Para quien lleva las botas, la lava que corre también sostiene, a la altura de su superficie.
 */
@Mixin(LiquidBlock.class)
public abstract class LiquidBlockMixin {
	@Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
	private void misfitrelics$flowingLavaSurface(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context,
			CallbackInfoReturnable<VoxelShape> cir) {
		if (!(context instanceof EntityCollisionContext entityContext)
				|| !(entityContext.getEntity() instanceof Player player)) {
			return;
		}
		FluidState fluid = state.getFluidState();
		if (!fluid.is(FluidTags.LAVA) || fluid.isSource() || !Mobility.walksOnLava(player)
				|| level.getFluidState(pos.above()).is(FluidTags.LAVA)) {
			return;
		}
		VoxelShape surface = Mobility.lavaSurface(fluid.getOwnHeight());
		if (context.isAbove(surface, pos, true)) {
			cir.setReturnValue(surface);
		}
	}
}
