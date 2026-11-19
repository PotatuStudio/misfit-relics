package io.github.potatustudio.misfitrelics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class LangTest {
	private static final Pattern NUMBERED = Pattern.compile("(.*\\.tooltip\\.)(\\d+)");

	@Test
	void inglesYEspanolTienenLasMismasClaves() {
		Map<String, String> en = LangFiles.read("en_us");
		Map<String, String> es = LangFiles.read("es_es");
		TreeSet<String> onlyEn = new TreeSet<>(en.keySet());
		onlyEn.removeAll(es.keySet());
		TreeSet<String> onlyEs = new TreeSet<>(es.keySet());
		onlyEs.removeAll(en.keySet());
		assertEquals(new TreeSet<String>(), onlyEn, "claves solo en inglés");
		assertEquals(new TreeSet<String>(), onlyEs, "claves solo en español");
	}

	/** Las líneas del tooltip se leen hasta la primera que falta: no puede haber huecos (tooltip.1, tooltip.3). */
	@Test
	void lineasDeTooltipSinHuecos() {
		for (Map<String, String> lang : LangFiles.both()) {
			for (String key : lang.keySet()) {
				Matcher m = NUMBERED.matcher(key);
				if (m.matches() && Integer.parseInt(m.group(2)) > 1) {
					String previous = m.group(1) + (Integer.parseInt(m.group(2)) - 1);
					assertTrue(lang.containsKey(previous), "falta " + previous + " antes de " + key);
				}
			}
		}
	}

	@Test
	void mismosMarcadoresDeFormatoEnLosDosIdiomas() {
		Map<String, String> en = LangFiles.read("en_us");
		Map<String, String> es = LangFiles.read("es_es");
		Pattern arg = Pattern.compile("%(\\d+\\$)?[sd]");
		for (String key : en.keySet()) {
			if (es.containsKey(key)) {
				assertEquals(count(arg, en.get(key)), count(arg, es.get(key)), "marcadores distintos en " + key);
			}
		}
	}

	private static long count(Pattern p, String s) {
		return p.matcher(s).results().count();
	}
}
