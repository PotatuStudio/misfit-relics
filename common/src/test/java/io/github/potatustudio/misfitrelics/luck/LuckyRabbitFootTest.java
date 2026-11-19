package io.github.potatustudio.misfitrelics.luck;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LuckyRabbitFootTest {
	@Test
	void pilasQueOcupaElBotinMultiplicado() {
		assertEquals(2, LuckyRabbitFoot.stacksFor(64, 64, 2));
		assertEquals(1, LuckyRabbitFoot.stacksFor(3, 16, 4));
		assertEquals(2, LuckyRabbitFoot.stacksFor(5, 16, 4));
		// Los objetos que no se apilan salen en copias.
		assertEquals(10, LuckyRabbitFoot.stacksFor(1, 1, 10));
		assertEquals(1, LuckyRabbitFoot.stacksFor(1, 64, 1));
	}

	@Test
	void unCofreTieneVeintisieteCasillas() {
		assertEquals(27, LuckyRabbitFoot.CHEST_SLOTS);
	}
}
