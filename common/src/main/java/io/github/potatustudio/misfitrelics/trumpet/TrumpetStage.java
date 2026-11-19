package io.github.potatustudio.misfitrelics.trumpet;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/**
 * Etapas de oxidación de la trompeta: mientras mejor cuidada, mejor funciona.
 * <p>
 * Como en un instrumento real, la corrosión vuelve el sonido más opaco y débil, así que cada golpe aturde menos.
 * Se recupera raspando con hacha y se conserva encerando con panal. Usa los sonidos de trompeta del bloque
 * musical (26.1), que también se apagan con la oxidación.
 *
 * @param stunTicks duración base del aturdimiento por golpe
 */
public enum TrumpetStage {
	COPPER(40, SoundEvents.NOTE_BLOCK_TRUMPET),
	EXPOSED(30, SoundEvents.NOTE_BLOCK_TRUMPET_EXPOSED),
	WEATHERED(20, SoundEvents.NOTE_BLOCK_TRUMPET_WEATHERED),
	OXIDIZED(12, SoundEvents.NOTE_BLOCK_TRUMPET_OXIDIZED);

	public final int stunTicks;
	public final Holder<SoundEvent> sound;

	TrumpetStage(int stunTicks, Holder<SoundEvent> sound) {
		this.stunTicks = stunTicks;
		this.sound = sound;
	}

	public TrumpetStage next() {
		return this == OXIDIZED ? OXIDIZED : values()[ordinal() + 1];
	}

	public TrumpetStage previous() {
		return this == COPPER ? COPPER : values()[ordinal() - 1];
	}
}
