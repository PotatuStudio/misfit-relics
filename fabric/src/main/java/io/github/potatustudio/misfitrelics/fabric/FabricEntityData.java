package io.github.potatustudio.misfitrelics.fabric;

import io.github.potatustudio.misfitrelics.relic.EntityData;
import java.util.IdentityHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

/** Datos de entidad en Fabric: attachments persistentes de Fabric API (mismos ids que antes del port). */
public final class FabricEntityData implements EntityData.Provider {
	private final Map<EntityData.Key<?>, AttachmentType<?>> types = new IdentityHashMap<>();

	public FabricEntityData() {
		for (EntityData.Key<?> key : EntityData.keys()) {
			types.put(key, create(key));
		}
	}

	private static <T> AttachmentType<T> create(EntityData.Key<T> key) {
		return AttachmentRegistry.create(key.id(), builder -> {
			builder.persistent(key.codec());
			if (key.copyOnDeath()) {
				builder.copyOnDeath();
			}
		});
	}

	@SuppressWarnings("unchecked")
	private <T> AttachmentType<T> type(EntityData.Key<T> key) {
		return (AttachmentType<T>) types.get(key);
	}

	@Override
	public <T> @Nullable T get(Entity entity, EntityData.Key<T> key) {
		return entity.getAttached(type(key));
	}

	@Override
	public <T> void set(Entity entity, EntityData.Key<T> key, T value) {
		entity.setAttached(type(key), value);
	}

	@Override
	public void remove(Entity entity, EntityData.Key<?> key) {
		entity.removeAttached(types.get(key));
	}
}
