package io.github.potatustudio.misfitrelics.client.jei;

import io.github.potatustudio.misfitrelics.MisfitRelics;
import io.github.potatustudio.misfitrelics.registry.ModItems;
import io.github.potatustudio.misfitrelics.registry.ModLoot;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Integración opcional con JEI: como las reliquias no se fabrican, cada una tiene una página de información con
 * dónde se encuentra y con qué probabilidad (sale de {@link ModLoot#sources}, la misma tabla que usa el botín).
 * Se carga por el entrypoint "jei_mod_plugin"; sin JEI instalado, esta clase nunca se usa.
 */
public class MisfitRelicsJeiPlugin implements IModPlugin {
	@Override
	public Identifier getPluginUid() {
		return MisfitRelics.id("jei_plugin");
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		for (Item item : BuiltInRegistries.ITEM) {
			if (!BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(MisfitRelics.MOD_ID)) {
				continue;
			}
			List<ModLoot.Source> sources = ModLoot.sources(item);
			if (sources.isEmpty()) {
				continue;
			}
			List<Component> lines = new ArrayList<>();
			lines.add(Component.translatable("jei.misfitrelics.where.title").withStyle(ChatFormatting.BOLD));
			for (ModLoot.Source source : sources) {
				lines.add(Component.translatable("jei.misfitrelics.where.entry",
						Component.translatable(source.translationKey()), formatChance(source.chance())));
			}
			lines.add(Component.translatable("jei.misfitrelics.where.note").withStyle(ChatFormatting.GRAY));
			registration.addItemStackInfo(new ItemStack(item), lines.toArray(Component[]::new));
		}
	}

	private static String formatChance(float chance) {
		float percent = chance * 100f;
		return percent == Math.round(percent) ? String.valueOf(Math.round(percent)) : String.format("%.1f", percent);
	}
}
