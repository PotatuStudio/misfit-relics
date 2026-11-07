package io.github.potatustudio.misfitrelics.stench;

import io.github.potatustudio.misfitrelics.registry.ModGameRules;
import io.github.potatustudio.misfitrelics.util.Allies;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;

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
		return !Allies.isAlly(level, target, sourceId);
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
