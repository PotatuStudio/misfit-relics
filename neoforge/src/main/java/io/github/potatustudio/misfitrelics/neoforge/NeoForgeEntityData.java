package io.github.potatustudio.misfitrelics.neoforge;

import io.github.potatustudio.misfitrelics.MisfitRelics;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jspecify.annotations.Nullable;

/** Datos de entidad en NeoForge: data attachments de NeoForge, con los mismos ids que en Fabric. */
public final class NeoForgeEntityData implements EntityData.Provider {
	private final Map<EntityData.Key<?>, Supplier<? extends AttachmentType<?>>> types = new IdentityHashMap<>();

	public NeoForgeEntityData(IEventBus modBus) {
		DeferredRegister<AttachmentType<?>> register = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MisfitRelics.MOD_ID);
		for (EntityData.Key<?> key : EntityData.keys()) {
			types.put(key, register.register(key.id().getPath(), () -> create(key)));
		}
		register.register(modBus);
	}

	private static <T> AttachmentType<T> create(EntityData.Key<T> key) {
		// Sin valor por defecto: solo se leen datos que existen (getExistingDataOrNull).
		AttachmentType.Builder<T> builder = AttachmentType.<T>builder(() -> {
			throw new IllegalStateException("Misfit Relics: " + key.id() + " no tiene valor por defecto");
		}).serialize(key.codec().fieldOf("value"));
		if (key.copyOnDeath()) {
			builder.copyOnDeath();
		}
		return builder.build();
	}

	@SuppressWarnings("unchecked")
	private <T> AttachmentType<T> type(EntityData.Key<T> key) {
		return (AttachmentType<T>) types.get(key).get();
	}

	@Override
	public <T> @Nullable T get(Entity entity, EntityData.Key<T> key) {
		return entity.getExistingDataOrNull(type(key));
	}

	@Override
	public <T> void set(Entity entity, EntityData.Key<T> key, T value) {
		entity.setData(type(key), value);
	}

	@Override
	public void remove(Entity entity, EntityData.Key<?> key) {
		entity.removeData(types.get(key).get());
	}
}
