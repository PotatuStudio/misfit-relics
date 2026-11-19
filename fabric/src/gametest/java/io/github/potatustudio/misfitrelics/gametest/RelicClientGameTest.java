package io.github.potatustudio.misfitrelics.gametest;

import eu.pb4.trinkets.api.SlotGroup;
import eu.pb4.trinkets.api.SlotType;
import eu.pb4.trinkets.api.TrinketAttachment;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketsApi;
import io.github.potatustudio.misfitrelics.MisfitRelics;
import io.github.potatustudio.misfitrelics.registry.ModAttachments;
import io.github.potatustudio.misfitrelics.registry.ModGameRules;
import io.github.potatustudio.misfitrelics.registry.ModItems;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import io.github.potatustudio.misfitrelics.relic.WornRelics;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.client.CameraType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * Prueba de humo del port: abre un mundo, revisa que los registros (gamerules, datos de entidad) funcionen, arma el
 * tooltip de cada reliquia, se pone cada accesorio (comprobando que {@link WornRelics} lo ve) y saca capturas de frente y
 * de espalda. Falla si algo de eso falla. Las entradas son del juego, nunca del sistema. Salida: carpeta misfit-test/
 * en el directorio de la ejecución (capturas y registro.txt).
 */
public class RelicClientGameTest implements FabricClientGameTest {
	private static final int Y = 150;
	private Path out;
	private PrintWriter log;
	private final List<String> failures = new ArrayList<>();

	@Override
	public void runTest(ClientGameTestContext ctx) {
		out = Path.of("misfit-test").toAbsolutePath();
		try {
			Files.createDirectories(out);
			log = new PrintWriter(Files.newBufferedWriter(out.resolve("registro.txt"), StandardCharsets.UTF_8), true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		try (TestSingleplayerContext sp = ctx.worldBuilder().create()) {
			TestServerContext srv = sp.getServer();
			setup(srv);
			sp.getConnection().waitForChunksRender();
			ctx.waitTicks(20);
			List<Item> relics = relics();
			log("reliquias: " + relics.size());
			registries(srv);
			tooltips(ctx, relics);
			worn(ctx, srv, relics);
			log(failures.isEmpty() ? "FIN: sin fallos" : "FIN: " + failures.size() + " fallos");
		} finally {
			log.close();
		}
		if (!failures.isEmpty()) {
			throw new AssertionError("Fallos: " + failures);
		}
	}

	private void setup(TestServerContext srv) {
		for (String c : List.of("gamerule advance_time false", "gamerule advance_weather false", "gamerule spawn_mobs false",
				"gamerule spawn_monsters false", "time set noon", "weather clear", "gamemode survival @a",
				"fill -12 " + (Y - 1) + " -12 12 " + (Y - 1) + " 12 minecraft:grass_block",
				"fill -12 " + Y + " -12 12 " + (Y + 8) + " 12 minecraft:air",
				"effect give @a minecraft:resistance infinite 4 true")) {
			srv.runCommand(c);
		}
		srv.runCommand(String.format(Locale.ROOT, "tp @a 0.5 %d 0.5 0 10", Y));
	}

	/** Gamerules propias registradas con su valor por defecto y datos de entidad que se guardan y se leen. */
	private void registries(TestServerContext srv) {
		srv.runOnServer(s -> {
			ServerPlayer p = player(s);
			check(p.level().getGameRules().get(ModGameRules.STENCH_CAN_KILL.get()), "gamerule stench_can_kill = true");
			check(p.level().getGameRules().get(ModGameRules.STENCH_CARRIER_CHANCE.get()) == 16, "gamerule stench_carrier_chance = 16");
			EntityData.set(p, ModAttachments.BAT_REVIVE, 42);
			check(EntityData.getOrElse(p, ModAttachments.BAT_REVIVE, 0) == 42, "dato de entidad bat_revive se guarda");
			EntityData.remove(p, ModAttachments.BAT_REVIVE);
			check(!EntityData.has(p, ModAttachments.BAT_REVIVE), "dato de entidad bat_revive se borra");
		});
	}

	private void tooltips(ClientGameTestContext ctx, List<Item> relics) {
		ctx.runOnClient(mc -> {
			for (Item item : relics) {
				try {
					int lines = new ItemStack(item).getTooltipLines(Item.TooltipContext.of(mc.level), mc.player, TooltipFlag.NORMAL).size();
					log("tooltip " + id(item) + ": " + lines + " líneas");
				} catch (Throwable t) {
					fail("tooltip " + id(item) + ": " + t);
				}
			}
		});
	}

	/** Cada accesorio puesto solo: WornRelics lo ve, y captura de frente y de espalda en tercera persona. */
	private void worn(ClientGameTestContext ctx, TestServerContext srv, List<Item> relics) {
		for (Item item : relics) {
			if (item == ModItems.RELIC_ICON.get()) {
				continue;
			}
			boolean[] equipped = {false};
			srv.runOnServer(s -> {
				ServerPlayer p = player(s);
				unequipAll(p);
				equipped[0] = equip(p, new ItemStack(item));
			});
			if (!equipped[0]) {
				continue;
			}
			ctx.waitTicks(3);
			srv.runOnServer(s -> check(WornRelics.isWearing(player(s), item), "WornRelics ve " + id(item) + " puesta"));
			ctx.runOnClient(mc -> mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
			shot(ctx, "puesta_" + id(item) + "_frente");
			ctx.runOnClient(mc -> mc.options.setCameraType(CameraType.THIRD_PERSON_BACK));
			shot(ctx, "puesta_" + id(item) + "_espalda");
		}
		srv.runOnServer(s -> unequipAll(player(s)));
		ctx.runOnClient(mc -> mc.options.setCameraType(CameraType.FIRST_PERSON));
	}

	// ---- Ayudantes ----------------------------------------------------------------------------------------------------

	private static List<Item> relics() {
		List<Item> list = new ArrayList<>();
		for (Item item : BuiltInRegistries.ITEM) {
			if (BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(MisfitRelics.MOD_ID)) {
				list.add(item);
			}
		}
		return list;
	}

	private static String id(Item item) {
		return BuiltInRegistries.ITEM.getKey(item).getPath();
	}

	private static ServerPlayer player(MinecraftServer s) {
		return s.getPlayerList().getPlayers().getFirst();
	}

	private void shot(ClientGameTestContext ctx, String name) {
		ctx.runOnClient(mc -> {
			if (mc.player != null) {
				float yaw = mc.player.getYRot();
				mc.player.yBodyRot = yaw;
				mc.player.yBodyRotO = yaw;
				mc.player.yHeadRot = yaw;
				mc.player.yHeadRotO = yaw;
			}
		});
		ctx.takeScreenshot(TestScreenshotOptions.of(name).withSize(640, 360).withDestinationDir(out).disableCounterPrefix());
	}

	private void check(boolean ok, String what) {
		if (ok) {
			log("OK " + what);
		} else {
			fail(what);
		}
	}

	private void fail(String what) {
		failures.add(what);
		log("FALLO " + what);
	}

	private void log(String s) {
		log.println(s);
	}

	private static List<SlotType> slotTypes(Player p) {
		List<SlotType> types = new ArrayList<>();
		for (SlotGroup g : TrinketsApi.getPlayerSlots(p).values()) {
			types.addAll(g.getSlots());
		}
		return types;
	}

	private static boolean equip(Player p, ItemStack stack) {
		TrinketAttachment att = TrinketsApi.getAttachment(p);
		for (SlotType t : slotTypes(p)) {
			TrinketSlotAccess access = att.getSlotAccess(t.getId(), 0);
			if (t.validatorCheck(stack, access, p)) {
				return access.set(stack.copy());
			}
		}
		return false;
	}

	private static void unequipAll(Player p) {
		TrinketAttachment att = TrinketsApi.getAttachment(p);
		for (SlotType t : slotTypes(p)) {
			for (int i = 0; i < t.amount(); i++) {
				att.getSlotAccess(t.getId(), i).set(ItemStack.EMPTY);
			}
		}
	}
}
