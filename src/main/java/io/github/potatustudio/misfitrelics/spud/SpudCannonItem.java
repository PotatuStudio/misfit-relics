package io.github.potatustudio.misfitrelics.spud;

import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Cañón de papas: se carga manteniendo clic derecho (0,5 s) y dispara al soltar, con retroceso.
 * La munición se busca primero en la otra mano y después en el inventario, como las flechas.
 * La animación de carga está en assets/misfitrelics/items/spud_cannon.json (usa CHARGE_TIME).
 */
public class SpudCannonItem extends Item {
	public static final int DURABILITY = 250;
	/** Ticks de carga necesarios para disparar (0,5 s). Si cambia, ajustar "scale" en el JSON del ítem. */
	public static final int CHARGE_TIME = 10;
	public static final int COOLDOWN = 5;
	public static final float VELOCITY = 2.4f;
	public static final float INACCURACY = 0.5f;
	/** Empuje hacia atrás al disparar. */
	public static final double RECOIL = 0.25;
	private static final int MAX_USE_DURATION = 72000;

	/** Munición válida. Más adelante: papa asada, papas impregnadas. */
	public static final Predicate<ItemStack> AMMO = stack -> stack.is(Items.POTATO) || stack.is(Items.POISONOUS_POTATO);

	public SpudCannonItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (findAmmo(player).isEmpty() && !player.hasInfiniteMaterials()) {
			return InteractionResult.FAIL;
		}
		player.startUsingItem(hand);
		return InteractionResult.CONSUME;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return MAX_USE_DURATION;
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.CROSSBOW;
	}

	@Override
	public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
		if (level.isClientSide()) {
			return;
		}
		int used = getUseDuration(stack, entity) - remainingUseDuration;
		if (used == 1) {
			playLoading(level, entity, SoundEvents.CROSSBOW_LOADING_START.value(), 0.9f);
		} else if (used == CHARGE_TIME / 2) {
			playLoading(level, entity, SoundEvents.CROSSBOW_LOADING_MIDDLE.value(), 0.9f);
		} else if (used == CHARGE_TIME) {
			playLoading(level, entity, SoundEvents.CROSSBOW_LOADING_END.value(), 0.8f);
		}
	}

	private static void playLoading(Level level, LivingEntity entity, SoundEvent sound, float pitch) {
		level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound, SoundSource.PLAYERS, 1.0f, pitch);
	}

	@Override
	public boolean releaseUsing(ItemStack cannon, Level level, LivingEntity entity, int timeLeft) {
		if (!(entity instanceof Player player) || getUseDuration(cannon, entity) - timeLeft < CHARGE_TIME) {
			return false;
		}
		ItemStack ammo = findAmmo(player);
		boolean infinite = player.hasInfiniteMaterials();
		if (ammo.isEmpty() && !infinite) {
			return false;
		}
		ItemStack shot = ammo.isEmpty() ? new ItemStack(Items.POISONOUS_POTATO) : ammo.copyWithCount(1);

		if (level instanceof ServerLevel serverLevel) {
			Projectile.spawnProjectileFromRotation(SpudProjectile::new, serverLevel, shot, player,
					0.0f, VELOCITY, INACCURACY);
			if (!infinite) {
				ammo.consume(1, player);
			}
			cannon.hurtAndBreak(1, player, player.getUsedItemHand());
		}
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.DISPENSER_LAUNCH, SoundSource.PLAYERS, 1.0f, 0.7f);
		// Retroceso: se aplica en ambos lados porque el cliente controla el movimiento del jugador.
		Vec3 recoil = player.getLookAngle().scale(-RECOIL);
		player.push(recoil.x, recoil.y * 0.5, recoil.z);
		player.getCooldowns().addCooldown(cannon, COOLDOWN);
		player.awardStat(Stats.ITEM_USED.get(this));
		return true;
	}

	/** Busca munición: otra mano primero, después el inventario. */
	private static ItemStack findAmmo(Player player) {
		ItemStack offhand = player.getOffhandItem();
		if (AMMO.test(offhand)) {
			return offhand;
		}
		Inventory inventory = player.getInventory();
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (AMMO.test(stack)) {
				return stack;
			}
		}
		return ItemStack.EMPTY;
	}
}
