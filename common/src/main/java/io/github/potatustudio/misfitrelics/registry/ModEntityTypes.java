package io.github.potatustudio.misfitrelics.registry;

import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.potatustudio.misfitrelics.MisfitRelics;
import io.github.potatustudio.misfitrelics.bat.AlliedBat;
import io.github.potatustudio.misfitrelics.bat.KingBat;
import io.github.potatustudio.misfitrelics.spud.SpudProjectile;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntityTypes {
	private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(MisfitRelics.MOD_ID, Registries.ENTITY_TYPE);

	/** Papa disparada por el cañón. */
	public static final RegistrySupplier<EntityType<SpudProjectile>> SPUD = ENTITIES.register("spud",
			() -> EntityType.Builder.<SpudProjectile>of(SpudProjectile::new, MobCategory.MISC)
					.sized(0.25f, 0.25f)
					.clientTrackingRange(4)
					.updateInterval(10)
					.build(key("spud")));

	/** Murciélago aliado del collar. */
	public static final RegistrySupplier<EntityType<AlliedBat>> ALLIED_BAT = ENTITIES.register("allied_bat",
			() -> EntityType.Builder.<AlliedBat>of(AlliedBat::new, MobCategory.MISC)
					.sized(0.5f, 0.9f)
					.clientTrackingRange(8)
					.build(key("allied_bat")));

	/** Rey murciélago: variante especial, más grande. */
	public static final RegistrySupplier<EntityType<KingBat>> KING_BAT = ENTITIES.register("king_bat",
			() -> EntityType.Builder.<KingBat>of(KingBat::new, MobCategory.MISC)
					.sized(0.85f, 1.5f)
					.clientTrackingRange(8)
					.build(key("king_bat")));

	private ModEntityTypes() {
	}

	private static ResourceKey<EntityType<?>> key(String path) {
		return ResourceKey.create(Registries.ENTITY_TYPE, MisfitRelics.id(path));
	}

	public static void init() {
		ENTITIES.register();
		EntityAttributeRegistry.register(ALLIED_BAT, AlliedBat::createAttributes);
		EntityAttributeRegistry.register(KING_BAT, KingBat::createKingAttributes);
	}
}
