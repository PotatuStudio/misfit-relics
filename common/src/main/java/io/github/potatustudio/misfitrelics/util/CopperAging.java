package io.github.potatustudio.misfitrelics.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Oxidación de los objetos de cobre del mod (trompeta y cañones), como los bloques de cobre vanilla:
 * se oxidan con el uso, el panal los encera (dejan de oxidarse) y el hacha quita la cera o una etapa de óxido.
 */
public final class CopperAging {
	private CopperAging() {
	}

	/**
	 * Con cierta probabilidad, reemplaza el objeto en la mano por su siguiente etapa de oxidación
	 * (conservando durabilidad y encantamientos). No hace nada si está encerado o ya es la última etapa.
	 */
	public static void maybeOxidize(Player player, InteractionHand hand, ItemStack stack, @Nullable Item nextStage,
			float chance) {
		if (nextStage == null || stack.isEmpty() || stack.has(DataComponents.WAXED)
				|| player.getRandom().nextFloat() >= chance) {
			return;
		}
		player.setItemInHand(hand, stack.transmuteCopy(nextStage));
	}

	/**
	 * Mantenimiento agachado, con la herramienta en la otra mano: panal para encerar, hacha para quitar
	 * la cera o una etapa de óxido.
	 *
	 * @param previousStage etapa anterior (menos oxidada), o null si ya está como nuevo
	 * @return PASS si no había nada que hacer
	 */
	public static InteractionResult tryMaintenance(Level level, Player player, InteractionHand hand, ItemStack stack,
			@Nullable Item previousStage) {
		InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
		ItemStack tool = player.getItemInHand(otherHand);
		boolean waxed = stack.has(DataComponents.WAXED);

		if (tool.is(Items.HONEYCOMB) && !waxed) {
			if (!level.isClientSide()) {
				stack.set(DataComponents.WAXED, Unit.INSTANCE);
				if (!player.hasInfiniteMaterials()) {
					tool.shrink(1);
				}
				play(level, player, SoundEvents.HONEYCOMB_WAX_ON, true);
			}
			return InteractionResult.SUCCESS;
		}

		if (tool.is(ItemTags.AXES) && (waxed || previousStage != null)) {
			if (!level.isClientSide()) {
				if (waxed) {
					stack.remove(DataComponents.WAXED);
					play(level, player, SoundEvents.AXE_WAX_OFF.value(), false);
				} else {
					player.setItemInHand(hand, stack.transmuteCopy(previousStage));
					play(level, player, SoundEvents.AXE_SCRAPE.value(), false);
				}
				tool.hurtAndBreak(1, player, otherHand);
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	private static void play(Level level, Player player, SoundEvent sound, boolean waxOn) {
		level.playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, 1.0f, 1.0f);
		if (level instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(waxOn ? ParticleTypes.WAX_ON : ParticleTypes.SCRAPE,
					player.getX(), player.getY() + 1.0, player.getZ(), 10, 0.4, 0.4, 0.4, 0.5);
		}
	}
}
