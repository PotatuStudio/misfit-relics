package io.github.potatustudio.misfitrelics.luck;

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
 * Pata de conejo de la suerte. Se equipa en el espacio "cinturón" de Trinkets (legs/belt); su efecto vive en
 * {@link LuckyRabbitFoot}. Se dibuja colgando de la cadera con assets/misfitrelics/trinkets/lucky_rabbit_foot.json.
 */
public class LuckyRabbitFootItem extends Item implements TrinketCallback {
	public LuckyRabbitFootItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
			Consumer<Component> tooltip, TooltipFlag flag) {
		tooltip.accept(Component.translatable("item.misfitrelics.lucky_rabbit_foot.tooltip.jackpot",
				Math.round(LuckyRabbitFoot.JACKPOT_CHANCE * 100), LuckyRabbitFoot.MIN_MULTIPLIER,
				LuckyRabbitFoot.MAX_MULTIPLIER).withStyle(ChatFormatting.GOLD));
		tooltip.accept(Component.translatable("item.misfitrelics.lucky_rabbit_foot.tooltip.sources")
				.withStyle(ChatFormatting.GRAY));
	}

	/** Al ponérsela: saltito de conejo y destellos. */
	@Override
	public void onEquip(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
		if (entity.level() instanceof ServerLevel level) {
			level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.RABBIT_JUMP,
					SoundSource.PLAYERS, 1.0f, 1.3f);
			level.sendParticles(ParticleTypes.HAPPY_VILLAGER, entity.getX(), entity.getY() + 0.8, entity.getZ(),
					10, 0.4, 0.3, 0.4, 0.0);
		}
	}
}
