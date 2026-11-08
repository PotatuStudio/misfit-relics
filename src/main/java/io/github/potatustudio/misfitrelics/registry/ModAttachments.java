package io.github.potatustudio.misfitrelics.registry;

import io.github.potatustudio.misfitrelics.MisfitRelics;
import io.github.potatustudio.misfitrelics.stench.StenchData;
import com.mojang.serialization.Codec;
import java.util.UUID;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.UUIDUtil;

public final class ModAttachments {
	/** Datos del Hedor en una entidad: quién lo originó, si puede contagiar y si es portador. */
	public static final AttachmentType<StenchData> STENCH = AttachmentRegistry.create(
			MisfitRelics.id("stench"),
			builder -> builder.persistent(StenchData.CODEC));

	/** Bicho aliado del Anillo de la plaga: UUID de su dueño. */
	public static final AttachmentType<UUID> PLAGUE_OWNER = AttachmentRegistry.create(
			MisfitRelics.id("plague_owner"),
			builder -> builder.persistent(UUIDUtil.CODEC));

	/**
	 * Ticks que faltan para que reviva el murciélago aliado. Va en el jugador (no en el collar) para que cambiar de
	 * collar no salte la espera; se conserva al morir.
	 */
	public static final AttachmentType<Integer> BAT_REVIVE = AttachmentRegistry.create(
			MisfitRelics.id("bat_revive"),
			builder -> builder.persistent(Codec.INT).copyOnDeath());

	private ModAttachments() {
	}

	public static void init() {
		// Fuerza la carga de la clase para registrar el attachment.
	}
}
