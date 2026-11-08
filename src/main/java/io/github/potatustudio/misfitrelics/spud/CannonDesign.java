package io.github.potatustudio.misfitrelics.spud;

import io.github.potatustudio.misfitrelics.util.CopperStage;

/**
 * Diseños del cañón de papas.
 * <ul>
 *   <li>{@link #RUSTIC}: improvisado, se craftea (tubos de cobre, pistón, pedernal, cuerda y palo).</li>
 *   <li>{@link #PRESSURE}: steampunk con manómetro, solo se encuentra en cofres; mejores estadísticas.</li>
 * </ul>
 * El óxido le quita daño, velocidad y precisión; se recupera raspando con hacha.
 *
 * @param baseId         id del ítem en cobre nuevo; las otras etapas llevan prefijo (exposed_, ...)
 * @param chargeTicks    ticks de carga; el JSON del ítem usa scale = 1 / chargeTicks para la animación
 * @param baseDamage     daño en cobre nuevo; baja 1 por etapa
 * @param baseVelocity   velocidad en cobre nuevo; baja 0,2 por etapa
 * @param baseInaccuracy imprecisión en cobre nuevo; sube 0,3 por etapa
 */
public enum CannonDesign {
	RUSTIC("spud_cannon", 10, 6.0f, 2.4f, 0.6f, 250),
	PRESSURE("pressure_spud_cannon", 8, 8.0f, 2.8f, 0.25f, 400);

	/** Probabilidad de oxidarse en cada disparo (3 %). */
	public static final float OXIDATION_CHANCE = 0.03f;

	public final String baseId;
	public final int chargeTicks;
	public final float baseDamage;
	public final float baseVelocity;
	public final float baseInaccuracy;
	public final int durability;

	CannonDesign(String baseId, int chargeTicks, float baseDamage, float baseVelocity, float baseInaccuracy,
			int durability) {
		this.baseId = baseId;
		this.chargeTicks = chargeTicks;
		this.baseDamage = baseDamage;
		this.baseVelocity = baseVelocity;
		this.baseInaccuracy = baseInaccuracy;
		this.durability = durability;
	}

	public String id(CopperStage stage) {
		return stage.idPrefix + baseId;
	}

	public float damage(CopperStage stage) {
		return baseDamage - stage.ordinal();
	}

	public float velocity(CopperStage stage) {
		return baseVelocity - 0.2f * stage.ordinal();
	}

	public float inaccuracy(CopperStage stage) {
		return baseInaccuracy + 0.3f * stage.ordinal();
	}
}
