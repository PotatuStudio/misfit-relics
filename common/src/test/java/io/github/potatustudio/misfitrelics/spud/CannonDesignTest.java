package io.github.potatustudio.misfitrelics.spud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.potatustudio.misfitrelics.trumpet.TrumpetStage;
import io.github.potatustudio.misfitrelics.util.CopperStage;
import io.github.potatustudio.misfitrelics.McBootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CannonDesignTest {
	@BeforeAll
	static void arrancarMinecraft() {
		McBootstrap.run();
	}

	@Test
	void elOxidoQuitaDañoVelocidadYPrecision() {
		for (CannonDesign design : CannonDesign.values()) {
			for (CopperStage stage : CopperStage.values()) {
				assertEquals(design.baseDamage - stage.ordinal(), design.damage(stage), 1e-6);
				assertTrue(design.damage(stage) > 0, design + " " + stage + " sigue haciendo daño");
				assertTrue(design.velocity(stage) > 0, design + " " + stage + " sigue disparando");
				if (stage != CopperStage.COPPER) {
					assertTrue(design.damage(stage) < design.damage(stage.previous()));
					assertTrue(design.velocity(stage) < design.velocity(stage.previous()));
					assertTrue(design.inaccuracy(stage) > design.inaccuracy(stage.previous()));
				}
			}
		}
		assertTrue(CannonDesign.PRESSURE.damage(CopperStage.COPPER) > CannonDesign.RUSTIC.damage(CopperStage.COPPER));
	}

	@Test
	void idsPorEtapa() {
		assertEquals("spud_cannon", CannonDesign.RUSTIC.id(CopperStage.COPPER));
		assertEquals("oxidized_pressure_spud_cannon", CannonDesign.PRESSURE.id(CopperStage.OXIDIZED));
	}

	@Test
	void etapasEnLosExtremos() {
		assertNull(CopperStage.OXIDIZED.next());
		assertNull(CopperStage.COPPER.previous());
		assertEquals(CopperStage.EXPOSED, CopperStage.COPPER.next());
		// La trompeta se queda en el extremo en vez de devolver null.
		assertEquals(TrumpetStage.OXIDIZED, TrumpetStage.OXIDIZED.next());
		assertEquals(TrumpetStage.COPPER, TrumpetStage.COPPER.previous());
		assertEquals(CopperStage.values().length, TrumpetStage.values().length);
	}
}
