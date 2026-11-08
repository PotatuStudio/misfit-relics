package io.github.potatustudio.misfitrelics.bat;

import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.callback.TrinketCallback;
import io.github.potatustudio.misfitrelics.registry.ModComponents;
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
 * Collar del murciélago. Se equipa en el espacio "collar" de Trinkets (chest/necklace);
 * sus poderes viven en {@link BatCollar}. Se dibuja en el pecho con assets/misfitrelics/trinkets/bat_collar.json.
 */
public class BatCollarItem extends Item implements TrinketCallback {
	private final boolean king;

	public BatCollarItem(boolean king, Properties properties) {
		super(properties);
		this.king = king;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
			Consumer<Component> tooltip, TooltipFlag flag) {
		tooltip.accept(Component.translatable("item.misfitrelics.bat_collar.tooltip.echolocation",
				Component.keybind(BatCollar.ECHOLOCATE_KEY)).withStyle(ChatFormatting.GOLD));
		tooltip.accept(Component.translatable("item.misfitrelics.bat_collar.tooltip.echolocation_stats",
				(int) BatCollar.ECHOLOCATION_RADIUS, BatCollar.ECHOLOCATION_GLOW / 20,
				BatCollar.ECHOLOCATION_COOLDOWN / 20).withStyle(ChatFormatting.GRAY));
		tooltip.accept(Component.translatable("item.misfitrelics.bat_collar.tooltip.lifesteal",
				Math.round(BatCollar.LIFESTEAL * 100)).withStyle(ChatFormatting.GOLD));
		if (king) {
			tooltip.accept(Component.translatable("item.misfitrelics.king_bat_collar.tooltip.bat",
					(int) KingBat.KING_BITE_DAMAGE, Math.round(KingBat.KING_LIFESTEAL * 100), BatCollar.KING_REVIVE_TICKS / 20)
					.withStyle(ChatFormatting.GOLD));
			tooltip.accept(Component.translatable("item.misfitrelics.king_bat_collar.tooltip.screech",
					KingBat.SCREECH_COOLDOWN / 20, KingBat.SCREECH_STUN / 20.0).withStyle(ChatFormatting.LIGHT_PURPLE));
		} else {
			tooltip.accept(Component.translatable("item.misfitrelics.bat_collar.tooltip.bat",
					(int) AlliedBat.BITE_DAMAGE, BatCollar.REVIVE_TICKS / 20).withStyle(ChatFormatting.GOLD));
		}
		Integer revive = stack.get(ModComponents.BAT_REVIVE);
		if (revive != null) {
			tooltip.accept(Component.translatable("item.misfitrelics.bat_collar.tooltip.reviving", (revive + 19) / 20)
					.withStyle(ChatFormatting.DARK_PURPLE));
		}
	}

	/** Mientras el murciélago revive, una barra morada se va llenando. */
	@Override
	public boolean isBarVisible(ItemStack stack) {
		return stack.has(ModComponents.BAT_REVIVE);
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		int left = stack.getOrDefault(ModComponents.BAT_REVIVE, 0);
		return Math.round(13.0f * (1.0f - (float) left / BatCollar.reviveTicks(stack)));
	}

	@Override
	public int getBarColor(ItemStack stack) {
		return 0x9A5FD0;
	}

	/** Al ponérselo: aleteo de murciélago y partículas, para que se note. */
	@Override
	public void onEquip(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
		if (entity.level() instanceof ServerLevel level) {
			level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.BAT_TAKEOFF,
					SoundSource.PLAYERS, 0.8f, 1.2f);
			level.sendParticles(ParticleTypes.SCULK_CHARGE_POP, entity.getX(), entity.getY() + 1.4, entity.getZ(),
					12, 0.4, 0.3, 0.4, 0.02);
		}
	}
}
