package io.github.potatustudio.misfitrelics.registry;

import io.github.potatustudio.misfitrelics.MisfitRelics;
import io.github.potatustudio.misfitrelics.stench.StenchData;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public final class ModAttachments {
	/** Datos del Hedor en una entidad: quién lo originó, si puede contagiar y si es portador. */
	public static final AttachmentType<StenchData> STENCH = AttachmentRegistry.create(
			MisfitRelics.id("stench"),
			builder -> builder.persistent(StenchData.CODEC));

	private ModAttachments() {
	}

	public static void init() {
		// Fuerza la carga de la clase para registrar el attachment.
	}
}
