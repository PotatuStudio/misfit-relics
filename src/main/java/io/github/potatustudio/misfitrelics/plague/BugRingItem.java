package io.github.potatustudio.misfitrelics.plague;

import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.callback.TrinketCallback;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Anillo de la plaga o del festín. Va en un espacio de anillo de Trinkets (cualquiera de las dos manos);
 * el efecto vive en {@link PlagueRings}.
 */
public class BugRingItem extends Item implements TrinketCallback {
	private final boolean feast;

	public BugRingItem(boolean feast, Properties properties) {
		super(properties);
		this.feast = feast;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
			Consumer<Component> tooltip, TooltipFlag flag) {
		if (feast) {
			tooltip.accept(Component.translatable("item.misfitrelics.feast_ring.tooltip.absorb",
					PlagueRings.HEAL / 2).withStyle(ChatFormatting.GOLD));
		} else {
			tooltip.accept(Component.translatable("item.misfitrelics.plague_ring.tooltip.spawn",
					Math.round(PlagueRings.SPAWN_CHANCE * 100), PlagueRings.MAX_BUGS).withStyle(ChatFormatting.GOLD));
			tooltip.accept(Component.translatable("item.misfitrelics.plague_ring.tooltip.fight",
					PlagueRings.FIGHT_TICKS / 20, PlagueRings.LIFETIME_TICKS / 20).withStyle(ChatFormatting.GRAY));
		}
		tooltip.accept(Component.translatable("item.misfitrelics.bug_ring.tooltip.combo").withStyle(ChatFormatting.DARK_GREEN));
	}

	@Override
	public void onEquip(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
		if (entity.level() instanceof ServerLevel level) {
			level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
					feast ? SoundEvents.ENDERMITE_AMBIENT : SoundEvents.SILVERFISH_AMBIENT, SoundSource.PLAYERS, 1.0f, 1.0f);
			level.sendParticles(ParticleTypes.SQUID_INK, entity.getX(), entity.getY() + 1.0, entity.getZ(),
					8, 0.3, 0.3, 0.3, 0.02);
		}
	}
}
