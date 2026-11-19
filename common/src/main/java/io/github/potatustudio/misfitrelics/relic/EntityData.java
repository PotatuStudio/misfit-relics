package io.github.potatustudio.misfitrelics.relic;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

/**
 * Datos propios guardados con una entidad (se guardan con el mundo). Independiente del loader: en Fabric son
 * attachments de Fabric API y en NeoForge, data attachments de NeoForge, con el mismo id, así que los mundos de Fabric
 * anteriores al port conservan sus datos. Cada loader instala su implementación antes de {@code MisfitRelics.init()}.
 */
public final class EntityData {
	/** Un dato: id, cómo se guarda y si pasa al jugador nuevo al morir. */
	public record Key<T>(Identifier id, Codec<T> codec, boolean copyOnDeath) {
	}

	/** Implementación de cada loader. Recibe todas las claves al instalarse (para registrarlas). */
	public interface Provider {
		<T> @Nullable T get(Entity entity, Key<T> key);

		<T> void set(Entity entity, Key<T> key, T value);

		void remove(Entity entity, Key<?> key);
	}

	private static final List<Key<?>> KEYS = new ArrayList<>();
	private static Provider provider;

	private EntityData() {
	}

	/** Declara un dato. Se llama desde inicializadores estáticos, antes de que el loader instale su proveedor. */
	public static <T> Key<T> key(Identifier id, Codec<T> codec, boolean copyOnDeath) {
		Key<T> key = new Key<>(id, codec, copyOnDeath);
		KEYS.add(key);
		return key;
	}

	public static List<Key<?>> keys() {
		return List.copyOf(KEYS);
	}

	public static void install(Provider implementation) {
		provider = Objects.requireNonNull(implementation);
	}

	private static Provider provider() {
		if (provider == null) {
			throw new IllegalStateException("Misfit Relics: falta instalar el proveedor de datos de entidad del loader");
		}
		return provider;
	}

	public static <T> @Nullable T get(Entity entity, Key<T> key) {
		return provider().get(entity, key);
	}

	public static <T> T getOrElse(Entity entity, Key<T> key, T fallback) {
		T value = get(entity, key);
		return value != null ? value : fallback;
	}

	public static boolean has(Entity entity, Key<?> key) {
		return get(entity, key) != null;
	}

	public static <T> void set(Entity entity, Key<T> key, T value) {
		provider().set(entity, key, value);
	}

	public static void remove(Entity entity, Key<?> key) {
		provider().remove(entity, key);
	}
}
