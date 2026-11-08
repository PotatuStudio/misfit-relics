package io.github.potatustudio.misfitrelics.registry;

import io.github.potatustudio.misfitrelics.MisfitRelics;
import io.github.potatustudio.misfitrelics.bat.AlliedBat;
import io.github.potatustudio.misfitrelics.bat.KingBat;
import io.github.potatustudio.misfitrelics.spud.SpudProjectile;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntityTypes {
	private static final ResourceKey<EntityType<?>> SPUD_KEY =
			ResourceKey.create(Registries.ENTITY_TYPE, MisfitRelics.id("spud"));

	/** Papa disparada por el cañón. */
	public static final EntityType<SpudProjectile> SPUD = Registry.register(BuiltInRegistries.ENTITY_TYPE, SPUD_KEY,
			EntityType.Builder.<SpudProjectile>of(SpudProjectile::new, MobCategory.MISC)
					.sized(0.25f, 0.25f)
					.clientTrackingRange(4)
					.updateInterval(10)
					.build(SPUD_KEY));

	private static final ResourceKey<EntityType<?>> ALLIED_BAT_KEY =
			ResourceKey.create(Registries.ENTITY_TYPE, MisfitRelics.id("allied_bat"));

	/** Murciélago aliado del collar. */
	public static final EntityType<AlliedBat> ALLIED_BAT = Registry.register(BuiltInRegistries.ENTITY_TYPE, ALLIED_BAT_KEY,
			EntityType.Builder.<AlliedBat>of(AlliedBat::new, MobCategory.MISC)
					.sized(0.5f, 0.9f)
					.clientTrackingRange(8)
					.build(ALLIED_BAT_KEY));

	private static final ResourceKey<EntityType<?>> KING_BAT_KEY =
			ResourceKey.create(Registries.ENTITY_TYPE, MisfitRelics.id("king_bat"));

	/** Rey murciélago: variante especial, más grande. */
	public static final EntityType<KingBat> KING_BAT = Registry.register(BuiltInRegistries.ENTITY_TYPE, KING_BAT_KEY,
			EntityType.Builder.<KingBat>of(KingBat::new, MobCategory.MISC)
					.sized(0.85f, 1.5f)
					.clientTrackingRange(8)
					.build(KING_BAT_KEY));

	private ModEntityTypes() {
	}

	public static void init() {
		FabricDefaultAttributeRegistry.register(ALLIED_BAT, AlliedBat.createAttributes());
		FabricDefaultAttributeRegistry.register(KING_BAT, KingBat.createKingAttributes());
	}
}
