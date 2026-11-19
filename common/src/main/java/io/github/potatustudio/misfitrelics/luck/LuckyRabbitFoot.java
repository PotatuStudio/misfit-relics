package io.github.potatustudio.misfitrelics.luck;

import io.github.potatustudio.misfitrelics.relic.WornRelics;
import io.github.potatustudio.misfitrelics.registry.ModItems;
import io.github.potatustudio.misfitrelics.util.Advancements;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Las dos patas de conejo (espacio cinturón de Trinkets, así que se lleva una u otra):
 * <ul>
 *   <li><b>De la suerte:</b> a menudo, un poco más de botín. Nunca resta.</li>
 *   <li><b>Maldita:</b> la apuesta. A veces multiplica el botín muchísimo; a veces lo hace desaparecer.</li>
 * </ul>
 * Aplican en cuatro fuentes, y en ninguna otra (recompensas de logros, trueques, regalos de gatos, etc.):
 * <ul>
 *   <li>minería: minerales ({@code c:ores}) y cultivos maduros que rompe quien la lleva. Si el bloque suelta su
 *       propio ítem (toque de seda), no aplica, así no se puede colocar y romper para duplicar;</li>
 *   <li>mobs que mata quien la lleva;</li>
 *   <li>cofres de estructuras, la primera vez que se abren;</li>
 *   <li>pesca.</li>
 * </ul>
 */
public final class LuckyRabbitFoot {
	/** De la suerte: probabilidad de duplicar el botín (25 %). */
	public static final float LUCKY_CHANCE = 0.25f;
	public static final int LUCKY_MULTIPLIER = 2;
	/** Maldita: probabilidad de jackpot (15 %) y de perderlo todo (10 %). */
	public static final float JACKPOT_CHANCE = 0.15f;
	public static final float LOSE_CHANCE = 0.10f;
	public static final int MIN_MULTIPLIER = 4;
	public static final int MAX_MULTIPLIER = 10;

	private LuckyRabbitFoot() {
	}

	/** Menas, con la etiqueta convencional que comparten Fabric y NeoForge. */
	private static final TagKey<Block> ORES = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "ores"));


	public static boolean isWearing(LivingEntity entity, Item foot) {
		return WornRelics.isWearing(entity, foot);
	}

	/** Lo llama {@code LootTableMixin} con el botín ya tirado de cualquier tabla. */
	public static void onDrops(Holder<LootTable> table, LootContext context, List<ItemStack> drops) {
		if (drops.isEmpty()) {
			return;
		}
		Player player = luckyPlayer(table, context, drops);
		if (player == null) {
			return;
		}
		RandomSource random = context.getRandom();
		if (isWearing(player, ModItems.CURSED_RABBIT_FOOT.get())) {
			float roll = random.nextFloat();
			if (roll < LOSE_CHANCE) {
				drops.clear();
				curse(context, player);
				Advancements.award(player, "cursed_loss", "lost");
			} else if (roll < LOSE_CHANCE + JACKPOT_CHANCE) {
				int multiplier = MIN_MULTIPLIER + random.nextInt(MAX_MULTIPLIER - MIN_MULTIPLIER + 1);
				multiply(drops, multiplier);
				jackpot(context, player, multiplier);
				if (multiplier == MAX_MULTIPLIER) {
					Advancements.award(player, "jackpot", "jackpot");
				}
			}
		} else if (isWearing(player, ModItems.LUCKY_RABBIT_FOOT.get()) && random.nextFloat() < LUCKY_CHANCE) {
			multiply(drops, LUCKY_MULTIPLIER);
			lucky(context, player);
		}
	}

	/** Quién se lleva el botín, si viene de una de las cuatro fuentes con suerte; null en cualquier otro caso. */
	private static @Nullable Player luckyPlayer(Holder<LootTable> table, LootContext context, List<ItemStack> drops) {
		Entity thisEntity = context.getOptional(LootContextParams.THIS_ENTITY);
		BlockState block = context.getOptional(LootContextParams.BLOCK_STATE);
		String path = table.unwrapKey().map(key -> key.identifier().getPath()).orElse("");

		if (block != null) {
			boolean lucky = block.is(ORES)
					|| (block.getBlock() instanceof CropBlock crop && crop.isMaxAge(block));
			boolean silkTouch = drops.stream().anyMatch(stack -> stack.is(block.getBlock().asItem()));
			return lucky && !silkTouch && thisEntity instanceof Player player ? player : null;
		}
		if (path.startsWith("chests/")) {
			return thisEntity instanceof Player player ? player : null;
		}
		if (path.startsWith("gameplay/fishing")) {
			return thisEntity instanceof FishingHook hook ? hook.getPlayerOwner() : null;
		}
		if (thisEntity instanceof LivingEntity && !(thisEntity instanceof Player)) {
			return context.getOptional(LootContextParams.LAST_DAMAGE_PLAYER);
		}
		return null;
	}

	/** Multiplica cada pila; lo que no cabe en una pila sale en pilas extra (y los objetos únicos, en copias). */
	private static void multiply(List<ItemStack> drops, int multiplier) {
		List<ItemStack> result = new ArrayList<>();
		for (ItemStack stack : drops) {
			int total = stack.getCount() * multiplier;
			int max = stack.getMaxStackSize();
			while (total > 0) {
				int count = Math.min(total, max);
				result.add(stack.copyWithCount(count));
				total -= count;
			}
		}
		drops.clear();
		drops.addAll(result);
	}

	private static Vec3 where(LootContext context, Player player) {
		Vec3 origin = context.getOptional(LootContextParams.ORIGIN);
		return origin != null ? origin : player.position();
	}

	private static void lucky(LootContext context, Player player) {
		ServerLevel level = context.getLevel();
		Vec3 at = where(context, player);
		level.playSound(null, at.x, at.y, at.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8f, 1.5f);
		level.sendParticles(ParticleTypes.HAPPY_VILLAGER, at.x, at.y + 0.5, at.z, 8, 0.4, 0.4, 0.4, 0.0);
	}

	private static void jackpot(LootContext context, Player player, int multiplier) {
		ServerLevel level = context.getLevel();
		Vec3 at = where(context, player);
		level.playSound(null, at.x, at.y, at.z, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 1.6f);
		level.playSound(null, at.x, at.y, at.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.2f);
		level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, at.x, at.y + 0.5, at.z, 25, 0.4, 0.4, 0.4, 0.3);
		level.sendParticles(ParticleTypes.HAPPY_VILLAGER, at.x, at.y + 0.5, at.z, 12, 0.5, 0.5, 0.5, 0.0);
		player.sendOverlayMessage(Component.translatable("item.misfitrelics.cursed_rabbit_foot.jackpot", multiplier)
				.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
	}

	private static void curse(LootContext context, Player player) {
		ServerLevel level = context.getLevel();
		Vec3 at = where(context, player);
		level.playSound(null, at.x, at.y, at.z, SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.PLAYERS, 0.8f, 0.8f);
		level.sendParticles(ParticleTypes.SOUL, at.x, at.y + 0.5, at.z, 10, 0.3, 0.3, 0.3, 0.02);
		level.sendParticles(ParticleTypes.LARGE_SMOKE, at.x, at.y + 0.5, at.z, 8, 0.3, 0.3, 0.3, 0.01);
		player.sendOverlayMessage(Component.translatable("item.misfitrelics.cursed_rabbit_foot.lost")
				.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
	}
}
