package io.github.potatustudio.misfitrelics.registry;

import com.mojang.serialization.Codec;
import io.github.potatustudio.misfitrelics.MisfitRelics;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import io.github.potatustudio.misfitrelics.stench.StenchData;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;

/** Datos propios guardados con las entidades (ver {@link EntityData}). */
public final class ModAttachments {
	/** Datos del Hedor en una entidad: quién lo originó, si puede contagiar y si es portador. */
	public static final EntityData.Key<StenchData> STENCH = EntityData.key(MisfitRelics.id("stench"), StenchData.CODEC, false);

	/** Bicho aliado del Anillo de la plaga: UUID de su dueño. */
	public static final EntityData.Key<UUID> PLAGUE_OWNER = EntityData.key(MisfitRelics.id("plague_owner"), UUIDUtil.CODEC, false);

	/** Bicho aliado del Anillo de la plaga: tick del mundo en que nació (su vida no se reinicia al recargar). */
	public static final EntityData.Key<Long> PLAGUE_BORN = EntityData.key(MisfitRelics.id("plague_born"), Codec.LONG, false);

	/**
	 * Ticks que faltan para que reviva el murciélago aliado. Va en el jugador (no en el collar) para que cambiar de
	 * collar no salte la espera; se conserva al morir.
	 */
	public static final EntityData.Key<Integer> BAT_REVIVE = EntityData.key(MisfitRelics.id("bat_revive"), Codec.INT, true);

	private ModAttachments() {
	}

	/** Fuerza la carga de la clase: las claves tienen que existir antes de que el loader instale su proveedor. */
	public static void init() {
	}
}
