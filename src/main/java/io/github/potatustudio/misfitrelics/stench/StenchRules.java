package io.github.potatustudio.misfitrelics.stench;

import io.github.potatustudio.misfitrelics.registry.ModGameRules;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;

/** Decide a quién puede afectar el Hedor según las game rules. */
public final class StenchRules {
	private StenchRules() {
	}

	/**
	 * @param source UUID de quien originó el Hedor; vacío si es natural (portadores), y entonces afecta a todos.
	 */
	public static boolean canAffect(ServerLevel level, LivingEntity target, Optional<UUID> source) {
		if (source.isEmpty()) {
			return true;
		}
		UUID sourceId = source.get();
		if (target.getUUID().equals(sourceId)) {
			return level.getGameRules().get(ModGameRules.STENCH_AFFECTS_SOURCE);
		}
		if (level.getGameRules().get(ModGameRules.STENCH_FRIENDLY_FIRE)) {
			return true;
		}
		return !isAlly(level, target, sourceId);
	}

	private static boolean isAlly(ServerLevel level, LivingEntity target, UUID sourceId) {
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
		// Para un jugador, los aldeanos y golems también son aliados.
		return source instanceof Player
				&& (target instanceof Npc || target instanceof IronGolem || target instanceof SnowGolem);
	}

	/**
	 * Resuelve quién originó un efecto: sigue la cadena de dueños de nubes y proyectiles
	 * (nube → poción → jugador) hasta llegar a una entidad viva.
	 */
	public static Optional<UUID> resolveSource(Entity effectSource) {
		Entity current = effectSource;
		for (int i = 0; i < 4 && current instanceof TraceableEntity traceable && !(current instanceof LivingEntity); i++) {
			current = traceable.getOwner();
		}
		return current == null ? Optional.empty() : Optional.of(current.getUUID());
	}
}
