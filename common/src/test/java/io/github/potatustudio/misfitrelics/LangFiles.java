package io.github.potatustudio.misfitrelics;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Los archivos de idioma del mod, leídos del código fuente (las pruebas corren en el directorio del módulo común). */
public final class LangFiles {
	private static final Path DIR = Path.of("src/main/resources/assets/misfitrelics/lang");

	private LangFiles() {
	}

	public static Map<String, String> read(String locale) {
		try {
			Map<String, String> out = new LinkedHashMap<>();
			JsonParser.parseString(Files.readString(DIR.resolve(locale + ".json"), StandardCharsets.UTF_8)).getAsJsonObject()
					.entrySet().forEach(e -> out.put(e.getKey(), e.getValue().getAsString()));
			return out;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static List<Map<String, String>> both() {
		return List.of(read("en_us"), read("es_es"));
	}
}
