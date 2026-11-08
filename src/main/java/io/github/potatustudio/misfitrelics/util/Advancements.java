package io.github.potatustudio.misfitrelics.util;

import io.github.potatustudio.misfitrelics.MisfitRelics;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Otorga los logros que ningún disparador vanilla detecta (los que en su JSON usan minecraft:impossible). */
public final class Advancements {
	private Advancements() {
	}

	public static void award(Player player, String advancement, String criterion) {
		if (player instanceof ServerPlayer serverPlayer) {
			AdvancementHolder holder = serverPlayer.level().getServer().getAdvancements().get(MisfitRelics.id(advancement));
			if (holder != null) {
				serverPlayer.getAdvancements().award(holder, criterion);
			}
		}
	}
}
