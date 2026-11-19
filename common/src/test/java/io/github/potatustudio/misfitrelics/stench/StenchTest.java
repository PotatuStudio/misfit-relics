package io.github.potatustudio.misfitrelics.stench;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import java.util.UUID;
import io.github.potatustudio.misfitrelics.McBootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class StenchTest {
	@BeforeAll
	static void arrancarMinecraft() {
		McBootstrap.run();
	}

	@Test
	void intervaloDeDañoBajaConElNivelYNuncaDeDiezTicks() {
		assertEquals(40, StenchEffect.damageInterval(0));
		assertEquals(20, StenchEffect.damageInterval(1));
		assertEquals(10, StenchEffect.damageInterval(2));
		assertEquals(10, StenchEffect.damageInterval(5));
		// Java usa solo 5 bits del desplazamiento: sin el tope, 32 volvía a 40.
		assertEquals(10, StenchEffect.damageInterval(31));
		assertEquals(10, StenchEffect.damageInterval(32));
		assertEquals(10, StenchEffect.damageInterval(36));
		assertEquals(10, StenchEffect.damageInterval(255));
	}

	@Test
	void datosDelHedorSeGuardanYSeLeenIgual() {
		StenchData original = new StenchData(Optional.of(UUID.fromString("00000000-0000-0000-0000-000000000042")), false, true);
		JsonElement json = StenchData.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
		assertEquals(original, StenchData.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
		assertEquals(StenchData.NONE, StenchData.CODEC.parse(JsonOps.INSTANCE,
				StenchData.CODEC.encodeStart(JsonOps.INSTANCE, StenchData.NONE).getOrThrow()).getOrThrow());
	}
}
