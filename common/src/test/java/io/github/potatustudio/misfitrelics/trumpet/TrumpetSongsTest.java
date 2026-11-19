package io.github.potatustudio.misfitrelics.trumpet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.potatustudio.misfitrelics.LangFiles;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TrumpetSongsTest {
	@Test
	void melodiasOrdenadasYConTonosDelBloqueMusical() {
		Set<String> keys = new HashSet<>();
		for (TrumpetSongs.Song song : TrumpetSongs.ALL) {
			assertTrue(keys.add(song.key()), "clave repetida: " + song.key());
			assertTrue(song.length() > 0);
			int last = -1;
			for (TrumpetSongs.Note note : song.notes()) {
				assertTrue(note.tick() >= last, song.key() + ": notas fuera de orden");
				assertTrue(note.pitch() >= 0 && note.pitch() <= 24, song.key() + ": tono fuera del bloque musical");
				assertTrue(note.volume() > 0);
				last = note.tick();
			}
		}
	}

	@Test
	void cadaMelodiaTieneNombreEnLosDosIdiomas() {
		for (Map<String, String> lang : LangFiles.both()) {
			for (TrumpetSongs.Song song : TrumpetSongs.ALL) {
				assertTrue(lang.containsKey("trumpet.misfitrelics.song." + song.key()), "falta el nombre de " + song.key());
			}
		}
		assertEquals(2, LangFiles.both().size());
	}
}
