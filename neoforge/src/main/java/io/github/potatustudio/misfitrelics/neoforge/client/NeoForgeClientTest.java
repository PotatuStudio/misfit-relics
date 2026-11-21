package io.github.potatustudio.misfitrelics.neoforge.client;

import io.github.potatustudio.misfitrelics.MisfitRelics;
import io.github.potatustudio.misfitrelics.relic.WornRelics;
import com.mojang.datafixers.util.Pair;
import io.github.potatustudio.misfitrelics.registry.ModItems;
import java.io.IOException;
import java.lang.reflect.Field;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

/**
 * Prueba automática de cliente en NeoForge, el equivalente de {@code :fabric:runClientGameTest} (NeoForge no trae una):
 * con la variable de entorno {@code RELIC_TEST=1} abre el mundo {@code saves/relictest}, arma el tooltip de cada
 * reliquia, se pone cada accesorio con Curios (comprobando que {@link WornRelics} lo ve) y saca capturas de frente y de
 * espalda; al terminar cierra el juego. Sin la variable no hace nada. Salida en run/relic-test (registro.txt y capturas
 * en run/screenshots/relic-test). Las entradas son del juego, nunca del sistema.
 */
public final class NeoForgeClientTest {
	private static final String WORLD = "relictest";
	private static final int Y = 150;
	private static final boolean GALLERY = "1".equals(System.getenv("RELIC_GALLERY"));

	private final Deque<Step> steps = new ArrayDeque<>();
	private final List<String> failures = new ArrayList<>();
	private PrintWriter log;
	private int wait;
	/** Mientras el servidor hace algo largo (preparar el escenario de la galería), los pasos esperan. */
	private volatile boolean busy;
	private boolean worldRequested;
	private int ticksInWorld;

	private record Step(int delay, Consumer<Minecraft> action) {
	}

	private NeoForgeClientTest() {
	}

	public static void initIfRequested() {
		if ("1".equals(System.getenv("RELIC_TEST"))) {
			NeoForgeClientTest test = new NeoForgeClientTest();
			NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> test.tick(Minecraft.getInstance()));
			MisfitRelics.LOGGER.info("Prueba automática de NeoForge activada (RELIC_TEST=1)");
		}
	}

	private void tick(Minecraft mc) {
		if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) {
			if (!worldRequested && mc.gui.overlay() == null && mc.gui.screen() != null) {
				worldRequested = true;
				mc.createWorldOpenFlows().openWorld(WORLD, () -> fail("no se pudo abrir el mundo " + WORLD));
			}
			return;
		}
		if (log == null) {
			start(mc);
		}
		if (++ticksInWorld < 100) {
			return;
		}
		if (busy) {
			return;
		}
		if (wait > 0) {
			wait--;
			return;
		}
		Step step = steps.poll();
		if (step == null) {
			return;
		}
		try {
			step.action().accept(mc);
		} catch (Throwable t) {
			fail("excepción: " + t);
			t.printStackTrace(log);
		}
		wait = step.delay();
	}

	private void start(Minecraft mc) {
		Path out = mc.gameDirectory.toPath().resolve("relic-test");
		try {
			Files.createDirectories(out);
			Files.createDirectories(mc.gameDirectory.toPath().resolve(Screenshot.SCREENSHOT_DIR).resolve("relic-test"));
			log = new PrintWriter(Files.newBufferedWriter(out.resolve("registro.txt"), StandardCharsets.UTF_8), true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		List<Item> relics = new ArrayList<>();
		for (Item item : BuiltInRegistries.ITEM) {
			if (BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(MisfitRelics.MOD_ID)) {
				relics.add(item);
			}
		}
		log("reliquias: " + relics.size());
		if (GALLERY) {
			gallery(mc);
			return;
		}
		onServer(s -> {
			for (String c : List.of("gamerule advance_time false", "gamerule advance_weather false", "gamerule spawn_mobs false",
					"time set noon", "weather clear", "gamemode survival @a",
					"fill -12 " + (Y - 1) + " -12 12 " + (Y - 1) + " 12 minecraft:grass_block",
					"fill -12 " + Y + " -12 12 " + (Y + 8) + " 12 minecraft:air",
					"effect give @a minecraft:resistance infinite 4 true", "tp @a 0.5 " + Y + " 0.5 0 10")) {
				s.getCommands().performPrefixedCommand(s.createCommandSourceStack(), c);
			}
		});
		steps.add(new Step(20, m -> tooltips(m, relics)));
		for (Item item : relics) {
			String id = BuiltInRegistries.ITEM.getKey(item).getPath();
			boolean[] equipped = {false};
			steps.add(new Step(6, m -> onServer(s -> {
				ServerPlayer p = s.getPlayerList().getPlayers().getFirst();
				unequipAll(p);
				equipped[0] = equip(p, new ItemStack(item));
			})));
			steps.add(new Step(0, m -> {
				if (equipped[0]) {
					onServer(s -> check(WornRelics.isWearing(s.getPlayerList().getPlayers().getFirst(), item), "WornRelics ve " + id + " puesta"));
				}
			}));
			steps.add(new Step(4, m -> {
				if (equipped[0]) {
					m.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
					alignBody(m);
				}
			}));
			steps.add(new Step(4, m -> {
				if (equipped[0]) {
					shot(m, "puesta_" + id + "_frente");
					m.options.setCameraType(CameraType.THIRD_PERSON_BACK);
					alignBody(m);
				}
			}));
			steps.add(new Step(2, m -> {
				if (equipped[0]) {
					shot(m, "puesta_" + id + "_espalda");
				}
			}));
		}
		steps.add(new Step(20, m -> {
			m.options.setCameraType(CameraType.FIRST_PERSON);
			onServer(s -> unequipAll(s.getPlayerList().getPlayers().getFirst()));
		}));
		steps.add(new Step(0, m -> {
			log(failures.isEmpty() ? "FIN: sin fallos" : "FIN: " + failures.size() + " fallos " + failures);
			log.close();
			m.stop();
			// En desarrollo, hilos de otros mods (Sodium, Iris) dejan colgado el apagado: si a los 30 s sigue vivo, se corta.
			Thread exit = new Thread(() -> {
				try {
					Thread.sleep(30_000);
				} catch (InterruptedException ignored) {
				}
				Runtime.getRuntime().halt(0);
			}, "relic-test-exit");
			exit.setDaemon(true);
			exit.start();
		}));
	}

	/**
	 * Modo galería ({@code RELIC_GALLERY=1}): fotos para CurseForge a 1920x1080 sin interfaz, en un prado abierto y a
	 * media tarde. La pestaña creativa del mod (la pantalla real), cada accesorio puesto, el murciélago aliado del collar
	 * y del collar del rey, y los cañones y la trompeta en la mano, todo con la cámara de frente. Capturas en
	 * run/screenshots/relic-test/galeria_*.png.
	 */
	private void gallery(Minecraft mc) {
		if (!mc.gui.hud.isHidden()) {
			mc.gui.hud.toggle();   // como F1
		}
		mc.options.guiScale().set(4);
		busy = true;
		onServer(s -> {
			try {
				stage(s);
			} catch (Throwable t) {
				fail("galería: " + t);
			} finally {
				busy = false;
			}
		});
		steps.add(new Step(200, m -> m.options.setCameraType(CameraType.THIRD_PERSON_FRONT)));
		// La pestaña creativa del mod: se elige antes de abrir la pantalla (el campo estático es el que usa al iniciarse).
		steps.add(new Step(30, m -> {
			try {
				Field tab = CreativeModeInventoryScreen.class.getDeclaredField("selectedTab");
				tab.setAccessible(true);
				tab.set(null, ModItems.TAB.get());
			} catch (ReflectiveOperationException e) {
				fail("pestaña creativa: " + e);
			}
			m.gui.setScreen(new CreativeModeInventoryScreen(m.player, m.player.connection.enabledFeatures(), false));
		}));
		// Sin tooltip: el juego cree que el mouse está en la esquina (solo el estado interno; el cursor real no se mueve).
		steps.add(new Step(5, m -> {
			try {
				for (String name : List.of("xpos", "ypos")) {
					Field f = MouseHandler.class.getDeclaredField(name);
					f.setAccessible(true);
					f.setDouble(m.mouseHandler, 1.0);
				}
			} catch (ReflectiveOperationException e) {
				fail("mouse: " + e);
			}
		}));
		steps.add(new Step(5, m -> shot(m, "galeria_pestana")));
		steps.add(new Step(10, m -> m.gui.setScreen(null)));
		for (Item item : ModItems.accessoryItems()) {
			String id = BuiltInRegistries.ITEM.getKey(item).getPath();
			boolean bat = id.contains("bat_collar");
			steps.add(new Step(bat ? 120 : 8, m -> onServer(s -> {
				ServerPlayer p = s.getPlayerList().getPlayers().getFirst();
				unequipAll(p);
				equip(p, new ItemStack(item));
			})));
			steps.add(new Step(30, m -> m.particleEngine.clearParticles()));
			steps.add(new Step(3, m -> shot(m, "galeria_puesta_" + id)));
		}
		for (String id : List.of("spud_cannon", "pressure_spud_cannon", "copper_trumpet")) {
			steps.add(new Step(10, m -> onServer(s -> {
				ServerPlayer p = s.getPlayerList().getPlayers().getFirst();
				unequipAll(p);
				p.setItemInHand(InteractionHand.MAIN_HAND,
						new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(MisfitRelics.MOD_ID, id))));
			})));
			steps.add(new Step(20, m -> m.particleEngine.clearParticles()));
			steps.add(new Step(3, m -> shot(m, "galeria_mano_" + id)));
		}
		steps.add(new Step(0, m -> {
			log(failures.isEmpty() ? "FIN galería" : "FIN galería: " + failures);
			log.close();
			m.stop();
			Thread exit = new Thread(() -> {
				try {
					Thread.sleep(30_000);
				} catch (InterruptedException ignored) {
				}
				Runtime.getRuntime().halt(0);
			}, "relic-test-exit");
			exit.setDaemon(true);
			exit.start();
		}));
	}

	/** Prado más cercano al spawn, con un claro parejo; el jugador queda en el centro mirando al sur. */
	private void stage(MinecraftServer s) {
		ServerLevel level = s.overworld();
		BlockPos from = new BlockPos(0, 64, 0);
		Pair<BlockPos, Holder<Biome>> found = level.findClosestBiome3d(biome -> List.of("minecraft:plains", "minecraft:meadow",
				"minecraft:sunflower_plains", "minecraft:savanna").stream().anyMatch(id -> biome.is(Identifier.parse(id))), from, 3000, 32, 64);
		BlockPos at = found != null ? found.getFirst() : from;
		int x = at.getX(), z = at.getZ();
		level.getChunk(x >> 4, z >> 4);   // la altura del suelo solo es real con el chunk generado
		int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
		int r = 22;
		for (int cx = (x - r - 16) >> 4; cx <= (x + r + 16) >> 4; cx++) {
			for (int cz = (z - r - 16) >> 4; cz <= (z + r + 16) >> 4; cz++) {
				level.getChunk(cx, cz);   // /fill solo cambia chunks cargados
			}
		}
		for (String c : List.of("gamerule advance_time false", "gamerule advance_weather false", "gamerule spawn_mobs false",
				"time set 10500", "weather clear", "gamemode creative @a", "clear @a", "kill @e[type=!minecraft:player]",
				"fill " + (x - r) + " " + (ground - 1) + " " + (z - r) + " " + (x + r) + " " + (ground - 1) + " " + (z + r) + " minecraft:grass_block",
				"fill " + (x - r) + " " + ground + " " + (z - r) + " " + (x + r) + " " + (ground + 12) + " " + (z + r) + " minecraft:air",
				"tp @a " + (x + 0.5) + " " + ground + " " + (z + 0.5) + " 0 0")) {
			s.getCommands().performPrefixedCommand(s.createCommandSourceStack(), c);
		}
		log("galería en " + x + " " + ground + " " + z);
	}

	private void tooltips(Minecraft mc, List<Item> relics) {
		for (Item item : relics) {
			String id = BuiltInRegistries.ITEM.getKey(item).getPath();
			try {
				int lines = new ItemStack(item).getTooltipLines(Item.TooltipContext.of(mc.level), mc.player, TooltipFlag.NORMAL).size();
				log("tooltip " + id + ": " + lines + " líneas");
			} catch (Throwable t) {
				fail("tooltip " + id + ": " + t);
			}
		}
	}

	private static void onServer(Consumer<MinecraftServer> action) {
		MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
		if (server != null) {
			server.execute(() -> action.accept(server));
		}
	}

	/** Primer espacio de Curios cuya etiqueta (curios:&lt;espacio&gt;) incluye el ítem. */
	private static boolean equip(ServerPlayer p, ItemStack stack) {
		ICuriosItemHandler inv = CuriosApi.getCuriosInventory(p).orElse(null);
		if (inv == null) {
			return false;
		}
		for (Map.Entry<String, ICurioStacksHandler> slot : inv.getCurios().entrySet()) {
			TagKey<Item> tag = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("curios", slot.getKey()));
			if (stack.is(tag) && slot.getValue().getSlots() > 0) {
				inv.setEquippedCurio(slot.getKey(), 0, stack.copy());
				return true;
			}
		}
		return false;
	}

	private static void unequipAll(ServerPlayer p) {
		CuriosApi.getCuriosInventory(p).ifPresent(inv -> inv.getCurios().forEach((id, handler) -> {
			for (int i = 0; i < handler.getSlots(); i++) {
				inv.setEquippedCurio(id, i, ItemStack.EMPTY);
			}
		}));
	}

	/** Tras un tp el cuerpo queda girado respecto de la cabeza: se alinea con la mirada antes de cada captura. */
	private static void alignBody(Minecraft mc) {
		if (mc.player != null) {
			float yaw = mc.player.getYRot();
			mc.player.yBodyRot = yaw;
			mc.player.yBodyRotO = yaw;
			mc.player.yHeadRot = yaw;
			mc.player.yHeadRotO = yaw;
		}
	}

	private void shot(Minecraft mc, String name) {
		alignBody(mc);
		Screenshot.grab(mc.gameDirectory, "relic-test/" + name + ".png", mc.gameRenderer.mainRenderTarget(), 1, message -> {
		});
	}

	private synchronized void check(boolean ok, String what) {
		if (ok) {
			log("OK " + what);
		} else {
			fail(what);
		}
	}

	private synchronized void fail(String what) {
		failures.add(what);
		log("FALLO " + what);
	}

	private synchronized void log(String s) {
		if (log != null) {
			log.println(s);
		} else {
			MisfitRelics.LOGGER.info("[prueba] {}", s);
		}
	}
}
