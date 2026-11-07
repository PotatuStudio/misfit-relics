package io.github.potatustudio.misfitrelics.util;

import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;

/** Qué cuenta como aliado de una entidad, para las reglas de daño a aliados del mod. */
public final class Allies {
	private Allies() {
	}

	/**
	 * Aliados de {@code sourceId}: sus mascotas, entidades de su mismo equipo y,
	 * si es un jugador, los aldeanos y golems.
	 */
	public static boolean isAlly(ServerLevel level, LivingEntity target, UUID sourceId) {
		if (target instanceof OwnableEntity ownable) {
			EntityReference<LivingEntity> owner = ownable.getOwnerReference();
			if (owner != null && sourceId.equals(owner.getUUID())) {
				return true;
			}
		}
		Entity source = level.getEntity(sourceId);
		if (source == null) {
			return false;
		}
		if (target.isAlliedTo(source)) {
			return true;
		}
		return source instanceof Player
				&& (target instanceof Npc || target instanceof IronGolem || target instanceof SnowGolem);
	}
}
