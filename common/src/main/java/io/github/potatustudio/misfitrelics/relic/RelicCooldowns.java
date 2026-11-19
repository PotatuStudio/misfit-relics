package io.github.potatustudio.misfitrelics.relic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.event.events.common.PlayerEvent;
import io.github.potatustudio.misfitrelics.MisfitRelics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Recargas de las reliquias guardadas con el mundo por jugador (como en Mythic Relics): la recarga de ítems de vanilla
 * no se guarda, así que sin esto salir y volver a entrar (o morir) la reiniciaba. Se muestran en el ícono como las de
 * vanilla y se reponen al entrar y al reaparecer. Se miden con el tiempo de juego del Overworld.
 */
public final class RelicCooldowns extends SavedData {
	private record Entry(UUID player, Identifier group, long until) {
		static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
				UUIDUtil.CODEC.fieldOf("player").forGetter(Entry::player),
				Identifier.CODEC.fieldOf("group").forGetter(Entry::group),
				Codec.LONG.fieldOf("until").forGetter(Entry::until)
		).apply(i, Entry::new));
	}

	private static final Codec<RelicCooldowns> CODEC = Entry.CODEC.listOf().xmap(RelicCooldowns::new, RelicCooldowns::entries);
	// Los datos son propios del mod: el tipo de corrección de vanilla no los toca (misma versión, sin reglas para ellos).
	private static final SavedDataType<RelicCooldowns> TYPE = new SavedDataType<>(MisfitRelics.id("cooldowns"), RelicCooldowns::new,
			CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

	private final Map<UUID, Map<Identifier, Entry>> byPlayer = new HashMap<>();

	private RelicCooldowns() {
	}

	private RelicCooldowns(List<Entry> list) {
		for (Entry e : list) {
			byPlayer.computeIfAbsent(e.player(), k -> new HashMap<>()).put(e.group(), e);
		}
	}

	private List<Entry> entries() {
		List<Entry> out = new ArrayList<>();
		byPlayer.values().forEach(m -> out.addAll(m.values()));
		return out;
	}

	public static void init() {
		// Al entrar y al reaparecer (el jugador nuevo no trae las recargas de vanilla), el ícono vuelve a mostrar lo que quedaba.
		PlayerEvent.PLAYER_JOIN.register(RelicCooldowns::restore);
		PlayerEvent.PLAYER_RESPAWN.register((player, conqueredEnd, removalReason) -> restore(player));
	}

	private static void restore(ServerPlayer player) {
		MinecraftServer server = player.level().getServer();
		Map<Identifier, Entry> mine = get(server).byPlayer.get(player.getUUID());
		if (mine == null) {
			return;
		}
		long now = now(server);
		for (Entry e : mine.values()) {
			if (e.until() > now) {
				player.getCooldowns().addCooldown(e.group(), (int) Math.min(Integer.MAX_VALUE, e.until() - now));
			}
		}
	}

	private static RelicCooldowns get(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(TYPE);
	}

	private static long now(MinecraftServer server) {
		return server.overworld().getGameTime();
	}

	/** Empieza la recarga del grupo de ítem: se ve en el ícono y se guarda con el mundo. */
	public static void start(ServerPlayer player, Identifier group, int ticks) {
		MinecraftServer server = player.level().getServer();
		RelicCooldowns data = get(server);
		long now = now(server);
		Map<Identifier, Entry> mine = data.byPlayer.computeIfAbsent(player.getUUID(), k -> new HashMap<>());
		mine.values().removeIf(e -> e.until() <= now);
		mine.put(group, new Entry(player.getUUID(), group, now + ticks));
		data.setDirty();
		player.getCooldowns().addCooldown(group, ticks);
	}
}
