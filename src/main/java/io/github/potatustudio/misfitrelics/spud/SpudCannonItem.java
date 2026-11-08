package io.github.potatustudio.misfitrelics.spud;

import io.github.potatustudio.misfitrelics.registry.ModItems;
import io.github.potatustudio.misfitrelics.util.CopperAging;
import io.github.potatustudio.misfitrelics.util.CopperStage;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Cañón de papas: se carga manteniendo clic derecho y dispara al soltar, con retroceso.
 * La munición se busca primero en la otra mano y después en el inventario, como las flechas.
 * Cada diseño ({@link CannonDesign}) tiene 4 ítems, uno por etapa de oxidación ({@link CopperStage}).
 */
public class SpudCannonItem extends Item {
	public static final int COOLDOWN = 5;
	/** Empuje hacia atrás al disparar. */
	public static final double RECOIL = 0.25;
	private static final int MAX_USE_DURATION = 72000;

	/** Munición válida. Más adelante: papa asada, papas impregnadas. */
	public static final Predicate<ItemStack> AMMO = stack -> stack.is(Items.POTATO) || stack.is(Items.POISONOUS_POTATO);

	private final CannonDesign design;
	private final CopperStage stage;

	public SpudCannonItem(CannonDesign design, CopperStage stage, Properties properties) {
		super(properties);
		this.design = design;
		this.stage = stage;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack cannon = player.getItemInHand(hand);
		if (player.isShiftKeyDown()) {
			CopperStage previous = stage.previous();
			InteractionResult maintenance = CopperAging.tryMaintenance(level, player, hand, cannon,
					previous == null ? null : ModItems.cannonFor(design, previous));
			if (maintenance != InteractionResult.PASS) {
				return maintenance;
			}
		}
		if (findAmmo(player).isEmpty() && !player.hasInfiniteMaterials()) {
			return InteractionResult.FAIL;
		}
		player.startUsingItem(hand);
		return InteractionResult.CONSUME;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
			Consumer<Component> tooltip, TooltipFlag flag) {
		float damage = design.damage(stage);
		tooltip.accept(Component.translatable("item.misfitrelics.spud_cannon.tooltip.damage",
				(int) damage, damage / 2).withStyle(ChatFormatting.GOLD));
		tooltip.accept(Component.translatable("item.misfitrelics.spud_cannon.tooltip.stench",
				(int) SpudProjectile.CLOUD_RADIUS, SpudProjectile.CLOUD_EFFECT_DURATION / 20).withStyle(ChatFormatting.GREEN));
		tooltip.accept(Component.translatable("item.misfitrelics.spud_cannon.tooltip.charge",
				design.chargeTicks / 20.0).withStyle(ChatFormatting.GRAY));
		if (stack.has(DataComponents.WAXED)) {
			tooltip.accept(Component.translatable("item.misfitrelics.copper.tooltip.waxed").withStyle(ChatFormatting.YELLOW));
		} else if (stage != CopperStage.OXIDIZED) {
			tooltip.accept(Component.translatable("item.misfitrelics.copper.tooltip.oxidizes",
					Math.round(CannonDesign.OXIDATION_CHANCE * 100)).withStyle(ChatFormatting.DARK_GREEN));
		}
		tooltip.accept(Component.translatable("item.misfitrelics.copper.tooltip.maintenance").withStyle(ChatFormatting.DARK_GRAY));
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
		} else if (used == design.chargeTicks / 2) {
			playLoading(level, entity, SoundEvents.CROSSBOW_LOADING_MIDDLE.value(), 0.9f);
		} else if (used == design.chargeTicks) {
			playLoading(level, entity, SoundEvents.CROSSBOW_LOADING_END.value(), 0.8f);
		}
	}

	private static void playLoading(Level level, LivingEntity entity, SoundEvent sound, float pitch) {
		level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound, SoundSource.PLAYERS, 1.0f, pitch);
	}

	@Override
	public boolean releaseUsing(ItemStack cannon, Level level, LivingEntity entity, int timeLeft) {
		if (!(entity instanceof Player player) || getUseDuration(cannon, entity) - timeLeft < design.chargeTicks) {
			return false;
		}
		ItemStack ammo = findAmmo(player);
		boolean infinite = player.hasInfiniteMaterials();
		if (ammo.isEmpty() && !infinite) {
			return false;
		}
		ItemStack shot = ammo.isEmpty() ? new ItemStack(Items.POISONOUS_POTATO) : ammo.copyWithCount(1);
		InteractionHand hand = player.getUsedItemHand();

		if (level instanceof ServerLevel serverLevel) {
			SpudProjectile spud = Projectile.spawnProjectileFromRotation(SpudProjectile::new, serverLevel, shot, player,
					0.0f, design.velocity(stage), design.inaccuracy(stage));
			spud.setImpactDamage(design.damage(stage));
			if (!infinite) {
				ammo.consume(1, player);
			}
			cannon.hurtAndBreak(1, player, hand);
			CopperStage next = stage.next();
			CopperAging.maybeOxidize(player, hand, cannon, next == null ? null : ModItems.cannonFor(design, next),
					CannonDesign.OXIDATION_CHANCE);
		}
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.DISPENSER_LAUNCH, SoundSource.PLAYERS, 1.0f, design == CannonDesign.PRESSURE ? 0.9f : 0.7f);
		// Retroceso: se aplica en ambos lados porque el cliente controla el movimiento del jugador.
		Vec3 recoil = player.getLookAngle().scale(-RECOIL);
		player.push(recoil.x, recoil.y * 0.5, recoil.z);
		// Si se oxidó en este disparo, el ítem en la mano ya es otro: el enfriamiento va al actual.
		ItemStack current = player.getItemInHand(hand);
		if (!current.isEmpty()) {
			player.getCooldowns().addCooldown(current, COOLDOWN);
		}
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
