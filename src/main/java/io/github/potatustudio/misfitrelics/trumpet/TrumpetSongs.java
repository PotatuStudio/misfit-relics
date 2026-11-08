package io.github.potatustudio.misfitrelics.trumpet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

/**
 * Melodías de la trompeta (clic derecho). Son puramente musicales: no aturden ni dañan.
 * Cada nota usa el sonido de trompeta del bloque musical de la etapa de oxidación, así que la misma
 * melodía suena más opaca mientras más oxidada esté la trompeta.
 */
public final class TrumpetSongs {
	/**
	 * Una nota: tick en que suena, tono del bloque musical (0 = Fa#, 12 = Fa# una octava arriba, 24 = dos octavas)
	 * y volumen (más de 1 solo aumenta el alcance).
	 */
	public record Note(int tick, int pitch, float volume) {
		static Note n(int tick, int pitch) {
			return new Note(tick, pitch, 2.0f);
		}

		static Note loud(int tick, int pitch) {
			return new Note(tick, pitch, 4.0f);
		}
	}

	/** @param key sufijo de la clave de traducción {@code trumpet.misfitrelics.song.<key>} */
	public record Song(String key, List<Note> notes) {
		public int length() {
			return notes.getLast().tick() + 1;
		}
	}

	/** Toque de carga de caballería. */
	public static final Song CHARGE = new Song("charge", List.of(
			Note.n(0, 1), Note.n(3, 6), Note.n(6, 10), Note.n(9, 13), Note.n(15, 10), Note.loud(18, 13)));
	/** Dos bocinazos secos. */
	public static final Song DOOT = new Song("doot", List.of(Note.loud(0, 6), Note.loud(6, 6)));
	/** Trombón triste: cuatro notas que bajan, la última arrastrada. */
	public static final Song SAD = new Song("sad", List.of(
			Note.n(0, 10), Note.n(8, 9), Note.n(16, 8), Note.n(24, 7), Note.n(27, 7), Note.n(30, 7), Note.n(33, 7)));
	/** Bocinazo a todo pulmón directo a la oreja: dos notas desafinadas a la vez, sostenidas. */
	public static final Song BLAST = new Song("blast", List.of(
			Note.loud(0, 6), Note.loud(0, 7), Note.loud(3, 6), Note.loud(3, 7), Note.loud(6, 6), Note.loud(6, 7),
			Note.loud(9, 6), Note.loud(9, 7), Note.loud(12, 5), Note.loud(12, 7)));
	/** Escala de calentamiento, subiendo y bajando. */
	public static final Song WARMUP = new Song("warmup", List.of(
			Note.n(0, 6), Note.n(2, 8), Note.n(4, 10), Note.n(6, 11), Note.n(8, 13), Note.n(10, 11), Note.n(12, 10),
			Note.n(14, 8), Note.n(16, 6)));

	public static final List<Song> ALL = List.of(CHARGE, DOOT, SAD, BLAST, WARMUP);

	private record Performance(ServerLevel level, LivingEntity player, Song song, Holder<SoundEvent> sound, int[] tick) {
	}

	private static final List<Performance> PLAYING = new ArrayList<>();

	private TrumpetSongs() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> tick());
	}

	public static Song random(RandomSource random) {
		return ALL.get(random.nextInt(ALL.size()));
	}

	public static void play(ServerLevel level, LivingEntity player, Song song, Holder<SoundEvent> sound) {
		PLAYING.removeIf(p -> p.player() == player);
		PLAYING.add(new Performance(level, player, song, sound, new int[] {0}));
		level.gameEvent(player, GameEvent.INSTRUMENT_PLAY, player.position());
	}

	private static void tick() {
		Iterator<Performance> it = PLAYING.iterator();
		while (it.hasNext()) {
			Performance p = it.next();
			if (!p.player().isAlive() || p.player().isRemoved() || p.tick()[0] >= p.song().length()) {
				it.remove();
				continue;
			}
			int now = p.tick()[0]++;
			for (Note note : p.song().notes()) {
				if (note.tick() == now) {
					playNote(p, note);
				}
			}
		}
	}

	private static void playNote(Performance p, Note note) {
		LivingEntity player = p.player();
		float pitch = (float) Math.pow(2.0, (note.pitch() - 12) / 12.0);
		p.level().playSound(null, player.getX(), player.getEyeY(), player.getZ(), p.sound().value(),
				SoundSource.RECORDS, note.volume(), pitch);
		Vec3 mouth = player.getEyePosition().add(player.getLookAngle().scale(0.8));
		// La partícula de nota toma su color de la velocidad X (0 a 1 recorre la paleta del bloque musical).
		p.level().sendParticles(ParticleTypes.NOTE, mouth.x, mouth.y, mouth.z, 0, note.pitch() / 24.0, 0, 0, 1);
	}
}
