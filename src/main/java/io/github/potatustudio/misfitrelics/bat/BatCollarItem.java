package io.github.potatustudio.misfitrelics.bat;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Collar del murciélago. Se equipa en el espacio "collar" de Trinkets (chest/necklace);
 * sus poderes viven en {@link BatCollar}.
 */
public class BatCollarItem extends Item {
	public BatCollarItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
			Consumer<Component> tooltip, TooltipFlag flag) {
		tooltip.accept(Component.translatable("item.misfitrelics.bat_collar.tooltip.echolocation",
				Component.keybind(BatCollar.ECHOLOCATE_KEY)).withStyle(ChatFormatting.GRAY));
		tooltip.accept(Component.translatable("item.misfitrelics.bat_collar.tooltip.lifesteal",
				Math.round(BatCollar.LIFESTEAL * 100)).withStyle(ChatFormatting.GRAY));
	}
}
