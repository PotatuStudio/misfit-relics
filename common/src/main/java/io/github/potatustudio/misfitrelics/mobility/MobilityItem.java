package io.github.potatustudio.misfitrelics.mobility;

import io.github.potatustudio.misfitrelics.relic.AccessoryHooks;
import io.github.potatustudio.misfitrelics.registry.ModLoot;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/** Reliquia de movilidad (ver {@link Mobility}). Cada tipo tiene sus líneas de tooltip y su sonido al ponérsela. */
public class MobilityItem extends Item implements AccessoryHooks {
	public enum Kind {
		SILK_GLOVES("silk_gloves", 2),
		WIDOW_GLOVES("widow_gloves", 3),
		BREEZE_BOTTLE("breeze_bottle", 2),
		MAGMA_BOOTS("magma_boots", 2),
		RAM_HORNS("ram_horns", 2);

		final String id;
		final int lines;

		Kind(String id, int lines) {
			this.id = id;
			this.lines = lines;
		}
	}

	private final Kind kind;

	public MobilityItem(Kind kind, Properties properties) {
		super(properties);
		this.kind = kind;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
			Consumer<Component> tooltip, TooltipFlag flag) {
		for (int i = 1; i <= kind.lines; i++) {
			Object[] args = switch (kind) {
				case SILK_GLOVES, WIDOW_GLOVES -> new Object[] {Mobility.WALL_JUMPS};
				case MAGMA_BOOTS -> new Object[] {Math.round(Mobility.MAGMA_NETHER_SPEED * 100)};
				case RAM_HORNS -> new Object[] {(int) Mobility.RAM_DAMAGE, Mobility.RAM_COOLDOWN / 20};
				default -> new Object[0];
			};
			tooltip.accept(Component.translatable("item.misfitrelics." + kind.id + ".tooltip." + i, args)
					.withStyle(i == 1 ? ChatFormatting.GOLD : ChatFormatting.GRAY));
		}
		ModLoot.appendWhere(this, tooltip);
	}

	@Override
	public void onEquip(ItemStack stack, LivingEntity entity) {
		if (entity.level() instanceof ServerLevel level) {
			SoundEvent sound = switch (kind) {
				case SILK_GLOVES, WIDOW_GLOVES -> SoundEvents.SPIDER_STEP;
				case BREEZE_BOTTLE -> SoundEvents.BREEZE_IDLE_AIR;
				case MAGMA_BOOTS -> SoundEvents.STRIDER_STEP_LAVA;
				case RAM_HORNS -> SoundEvents.GOAT_PREPARE_RAM;
			};
			level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound, SoundSource.PLAYERS, 1.0f, 1.0f);
		}
	}
}
