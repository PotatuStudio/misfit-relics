package io.github.potatustudio.misfitrelics;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

/** Arranca los registros de Minecraft para las pruebas que cargan clases del juego (sonidos, efectos). */
public final class McBootstrap {
	private static boolean done;

	private McBootstrap() {
	}

	public static synchronized void run() {
		if (!done) {
			SharedConstants.tryDetectVersion();
			Bootstrap.bootStrap();
			done = true;
		}
	}
}
