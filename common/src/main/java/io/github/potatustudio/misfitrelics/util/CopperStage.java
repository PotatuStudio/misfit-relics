package io.github.potatustudio.misfitrelics.util;

/** Etapas de oxidación del cobre, en el mismo orden que los bloques vanilla. */
public enum CopperStage {
	COPPER(""),
	EXPOSED("exposed_"),
	WEATHERED("weathered_"),
	OXIDIZED("oxidized_");

	/** Prefijo del id del ítem en esta etapa (vacío para el cobre nuevo). */
	public final String idPrefix;

	CopperStage(String idPrefix) {
		this.idPrefix = idPrefix;
	}

	public CopperStage next() {
		return this == OXIDIZED ? null : values()[ordinal() + 1];
	}

	public CopperStage previous() {
		return this == COPPER ? null : values()[ordinal() - 1];
	}
}
