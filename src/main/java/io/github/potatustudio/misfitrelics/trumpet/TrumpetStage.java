package io.github.potatustudio.misfitrelics.trumpet;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/**
 * Etapas de oxidación de la trompeta. El cobre brillante suena fuerte y lejos con aturdimiento corto;
 * a medida que se oxida, el sonido se apaga: menos alcance, cono más ancho, aturdimiento más largo
 * y enfriamiento más largo. Usa los sonidos de trompeta del bloque musical (26.1).
 *
 * @param range        alcance en bloques
 * @param coneDegrees  apertura total del cono
 * @param stunTicks    duración base del aturdimiento
 * @param cooldown     enfriamiento entre usos, en ticks
 */
public enum TrumpetStage {
	COPPER(10.0, 60.0, 20, 60, SoundEvents.NOTE_BLOCK_TRUMPET),
	EXPOSED(9.0, 70.0, 25, 80, SoundEvents.NOTE_BLOCK_TRUMPET_EXPOSED),
	WEATHERED(8.0, 80.0, 30, 100, SoundEvents.NOTE_BLOCK_TRUMPET_WEATHERED),
	OXIDIZED(7.0, 90.0, 40, 120, SoundEvents.NOTE_BLOCK_TRUMPET_OXIDIZED);

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
