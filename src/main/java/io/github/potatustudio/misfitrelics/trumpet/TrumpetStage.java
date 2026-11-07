package io.github.potatustudio.misfitrelics.trumpet;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/**
 * Etapas de oxidación de la trompeta: mientras mejor cuidada, mejor funciona.
 * <p>
 * Como en un instrumento real, la corrosión vuelve el sonido más opaco y débil: pierde agudos, así que
 * llega menos lejos pero se dispersa más (cono más ancho), y aturde menos. Las válvulas trabadas
 * alargan un poco el enfriamiento. Se recupera raspando con hacha y se conserva encerando con panal.
 * Usa los sonidos de trompeta del bloque musical (26.1), que también se apagan con la oxidación.
 *
 * @param range        alcance en bloques
 * @param coneDegrees  apertura total del cono
 * @param stunTicks    duración base del aturdimiento
 * @param cooldown     enfriamiento entre usos, en ticks
 */
public enum TrumpetStage {
	COPPER(10.0, 60.0, 40, 60, SoundEvents.NOTE_BLOCK_TRUMPET),
	EXPOSED(9.0, 70.0, 30, 60, SoundEvents.NOTE_BLOCK_TRUMPET_EXPOSED),
	WEATHERED(7.0, 80.0, 20, 70, SoundEvents.NOTE_BLOCK_TRUMPET_WEATHERED),
	OXIDIZED(5.0, 90.0, 12, 80, SoundEvents.NOTE_BLOCK_TRUMPET_OXIDIZED);

	public final double range;
	public final double coneDegrees;
	public final int stunTicks;
	public final int cooldown;
	public final Holder<SoundEvent> sound;

	TrumpetStage(double range, double coneDegrees, int stunTicks, int cooldown, Holder<SoundEvent> sound) {
		this.range = range;
		this.coneDegrees = coneDegrees;
		this.stunTicks = stunTicks;
		this.cooldown = cooldown;
		this.sound = sound;
	}

	public TrumpetStage next() {
		return this == OXIDIZED ? OXIDIZED : values()[ordinal() + 1];
	}

	public TrumpetStage previous() {
		return this == COPPER ? COPPER : values()[ordinal() - 1];
	}
}
