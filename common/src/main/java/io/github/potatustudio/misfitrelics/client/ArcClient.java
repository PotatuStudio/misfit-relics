package io.github.potatustudio.misfitrelics.client;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import io.github.potatustudio.misfitrelics.network.ArcPayload;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Dibuja los arcos eléctricos del guantelete y del corazón de creeper: rayo quebrado (desplazamiento del punto medio)
 * con chispas eléctricas; como cada tick sale otra forma, el rayo parpadea. La "corriente" es una chispa brillante que
 * recorre el arco del origen al objetivo a lo largo de su duración.
 */
public final class ArcClient {
	private static final class Arc {
		final ArcPayload payload;
		int age;

		Arc(ArcPayload payload) {
			this.payload = payload;
		}
	}

	private static final List<Arc> ARCS = new ArrayList<>();

	private ArcClient() {
	}

	public static void init() {
		NetworkManager.registerReceiver(NetworkManager.s2c(), ArcPayload.TYPE, ArcPayload.CODEC,
				(payload, context) -> context.queue(() -> {
					Arc arc = new Arc(payload);
					ARCS.add(arc);
					draw(arc);
				}));
		ClientTickEvent.CLIENT_POST.register(ArcClient::tick);
	}

	private static void tick(Minecraft client) {
		if (client.level == null) {
			ARCS.clear();
			return;
		}
		Iterator<Arc> it = ARCS.iterator();
		while (it.hasNext()) {
			Arc arc = it.next();
			if (++arc.age >= arc.payload.ticks() || !draw(arc)) {
				it.remove();
			}
		}
	}

	/** Dibuja el arco en su estado actual; false si una de sus entidades ya no está. */
	private static boolean draw(Arc arc) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null) {
			return false;
		}
		Vec3 start = end(level, arc.payload.fromId(), arc.payload.from());
		Vec3 end = end(level, arc.payload.toId(), arc.payload.to());
		if (start == null || end == null) {
			return false;
		}
		RandomSource random = level.getRandom();
		List<Vec3> points = new ArrayList<>(List.of(start, end));
		double offset = start.distanceTo(end) * 0.22;
		for (int pass = 0; pass < 3; pass++) {
			List<Vec3> refined = new ArrayList<>();
			for (int i = 0; i < points.size() - 1; i++) {
				Vec3 p = points.get(i);
				Vec3 q = points.get(i + 1);
				refined.add(p);
				refined.add(p.add(q).scale(0.5).add((random.nextDouble() - 0.5) * offset, (random.nextDouble() - 0.5) * offset,
						(random.nextDouble() - 0.5) * offset));
			}
			refined.add(points.getLast());
			points = refined;
			offset *= 0.5;
		}
		for (int i = 0; i < points.size() - 1; i++) {
			Vec3 p = points.get(i);
			Vec3 q = points.get(i + 1);
			int steps = Math.max(1, (int) (p.distanceTo(q) / 0.26));
			for (int k = 0; k < steps; k++) {
				Vec3 t = p.add(q.subtract(p).scale((double) k / steps));
				level.addParticle(ParticleTypes.ELECTRIC_SPARK, t.x, t.y, t.z, 0, 0, 0);
			}
		}
		double f = Math.min(1.0, (arc.age + 1) / (double) arc.payload.ticks());
		Vec3 head = start.add(end.subtract(start).scale(f));
		level.addParticle(ParticleTypes.END_ROD, head.x, head.y, head.z, 0, 0, 0);
		for (int i = 0; i < 2; i++) {
			level.addParticle(ParticleTypes.ELECTRIC_SPARK, head.x + random.nextGaussian() * 0.06, head.y + random.nextGaussian() * 0.06,
					head.z + random.nextGaussian() * 0.06, random.nextGaussian() * 0.08, random.nextGaussian() * 0.08,
					random.nextGaussian() * 0.08);
		}
		return true;
	}

	private static @Nullable Vec3 end(ClientLevel level, int id, Vec3 fallback) {
		if (id < 0) {
			return fallback;
		}
		Entity entity = level.getEntity(id);
		return entity == null || entity.isRemoved() ? null : entity.getBoundingBox().getCenter();
	}
}
