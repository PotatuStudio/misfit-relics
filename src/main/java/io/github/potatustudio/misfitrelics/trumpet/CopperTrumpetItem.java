package io.github.potatustudio.misfitrelics.trumpet;

import io.github.potatustudio.misfitrelics.registry.ModEffects;
import io.github.potatustudio.misfitrelics.registry.ModGameRules;
import io.github.potatustudio.misfitrelics.registry.ModItems;
import io.github.potatustudio.misfitrelics.util.Allies;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Trompeta de cobre: lanza un cono de sonido que aturde.
 * <ul>
 *   <li>Eco: si el objetivo está contra una pared, el aturdimiento dura un 50 % más; bajo techo, un 25 % más.</li>
 *   <li>Oxidación: cada uso tiene una probabilidad de pasar a la siguiente etapa, salvo que esté encerada.</li>
 *   <li>Agachado con panal en la otra mano: la encera. Agachado con hacha: quita la cera o una etapa de óxido.</li>
 * </ul>
 */
public class CopperTrumpetItem extends Item {
	public static final int DURABILITY = 200;
	/** Probabilidad de oxidarse en cada uso (4 %). */
	public static final float OXIDATION_CHANCE = 0.04f;
	public static final float WALL_ECHO_MULTIPLIER = 1.5f;
	public static final float ROOF_ECHO_MULTIPLIER = 1.25f;
	public static final double KNOCKBACK = 0.4;
	private static final int USE_DURATION = 20;

	private final TrumpetStage stage;

	public CopperTrumpetItem(TrumpetStage stage, Properties properties) {
		super(properties);
		this.stage = stage;
	}

	public TrumpetStage stage() {
		return stage;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack trumpet = player.getItemInHand(hand);
		if (player.isShiftKeyDown()) {
			InteractionResult maintenance = tryMaintenance(level, player, hand, trumpet);
			if (maintenance != InteractionResult.PASS) {
				return maintenance;
			}
		}

		level.playSound(null, player.getX(), player.getY(), player.getZ(), stage.sound.value(),
				SoundSource.PLAYERS, 3.0f, 1.0f);
		if (level instanceof ServerLevel serverLevel) {
			blast(serverLevel, player);
			trumpet.hurtAndBreak(1, player, hand);
			if (!trumpet.isEmpty()) {
				maybeOxidize(player, hand, trumpet);
			}
		}
		// Si se oxidó en este uso, el ítem en la mano ya es otro: el enfriamiento va al actual.
		ItemStack current = player.getItemInHand(hand);
		if (!current.isEmpty()) {
			player.getCooldowns().addCooldown(current, stage.cooldown);
		}
		player.awardStat(Stats.ITEM_USED.get(this));
		player.startUsingItem(hand);
		return InteractionResult.CONSUME;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return USE_DURATION;
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.TOOT_HORN;
	}

	/** Cono de sonido: aturde y empuja a las entidades vivas visibles dentro del cono. */
	private void blast(ServerLevel level, Player player) {
		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getLookAngle();
		double minDot = Math.cos(Math.toRadians(stage.coneDegrees / 2.0));
		boolean friendlyFire = level.getGameRules().get(ModGameRules.TRUMPET_FRIENDLY_FIRE);
		AABB area = player.getBoundingBox().inflate(stage.range);

		for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area,
				t -> t != player && t.isAlive() && !t.isSpectator())) {
			Vec3 toTarget = target.getBoundingBox().getCenter().subtract(eye);
			double distance = toTarget.length();
			if (distance > stage.range || distance < 0.01) {
				continue;
			}
			Vec3 direction = toTarget.normalize();
			if (direction.dot(look) < minDot || !player.hasLineOfSight(target)) {
				continue;
			}
			if (target instanceof Player other && other.isCreative()) {
				continue;
			}
			if (!friendlyFire && Allies.isAlly(level, target, player.getUUID())) {
				continue;
			}
			stun(level, player, target, direction);
		}
		spawnConeParticles(level, eye, look);
	}

	private void stun(ServerLevel level, Player player, LivingEntity target, Vec3 direction) {
		float duration = stage.stunTicks;
		boolean wallEcho = isAgainstWall(level, target, direction);
		if (wallEcho) {
			duration *= WALL_ECHO_MULTIPLIER;
		}
		if (!level.canSeeSky(target.blockPosition())) {
			duration *= ROOF_ECHO_MULTIPLIER;
		}
		target.addEffect(new MobEffectInstance(ModEffects.STUNNED, Math.round(duration)), player);
		target.push(direction.x * KNOCKBACK, 0.1, direction.z * KNOCKBACK);
		target.needsSync = true;

		Vec3 center = target.getBoundingBox().getCenter();
		level.sendParticles(wallEcho ? ParticleTypes.ELECTRIC_SPARK : ParticleTypes.CRIT,
				center.x, center.y + target.getBbHeight() * 0.3, center.z, wallEcho ? 12 : 6, 0.3, 0.3, 0.3, 0.05);
	}

	/** Hay un bloque sólido justo detrás del objetivo, en la dirección del sonido. */
	private static boolean isAgainstWall(ServerLevel level, LivingEntity target, Vec3 direction) {
		Vec3 behind = target.getBoundingBox().getCenter().add(direction.scale(target.getBbWidth() / 2.0 + 0.6));
		return level.getBlockState(BlockPos.containing(behind)).isSolidRender();
	}

	private void spawnConeParticles(ServerLevel level, Vec3 eye, Vec3 look) {
		for (int d = 1; d <= (int) stage.range; d++) {
			Vec3 point = eye.add(look.scale(d));
			double spread = d * Math.tan(Math.toRadians(stage.coneDegrees / 2.0)) * 0.5;
			level.sendParticles(ParticleTypes.NOTE, point.x, point.y, point.z, 2, spread, spread * 0.5, spread, 0.0);
		}
	}

	private void maybeOxidize(Player player, InteractionHand hand, ItemStack trumpet) {
		if (stage == TrumpetStage.OXIDIZED || trumpet.has(DataComponents.WAXED)
				|| player.getRandom().nextFloat() >= OXIDATION_CHANCE) {
			return;
		}
		player.setItemInHand(hand, trumpet.transmuteCopy(ModItems.trumpetFor(stage.next())));
	}

	/** Mantenimiento agachado: panal para encerar, hacha para quitar cera u óxido. */
	private InteractionResult tryMaintenance(Level level, Player player, InteractionHand hand, ItemStack trumpet) {
		InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
		ItemStack tool = player.getItemInHand(otherHand);

		if (tool.is(Items.HONEYCOMB) && !trumpet.has(DataComponents.WAXED)) {
			if (!level.isClientSide()) {
				trumpet.set(DataComponents.WAXED, Unit.INSTANCE);
				if (!player.hasInfiniteMaterials()) {
					tool.shrink(1);
				}
				playMaintenance(level, player, SoundEvents.HONEYCOMB_WAX_ON, true);
			}
			return InteractionResult.SUCCESS;
		}

		if (tool.is(ItemTags.AXES) && (trumpet.has(DataComponents.WAXED) || stage != TrumpetStage.COPPER)) {
			if (!level.isClientSide()) {
				if (trumpet.has(DataComponents.WAXED)) {
					trumpet.remove(DataComponents.WAXED);
					playMaintenance(level, player, SoundEvents.AXE_WAX_OFF.value(), false);
				} else {
					player.setItemInHand(hand, trumpet.transmuteCopy(ModItems.trumpetFor(stage.previous())));
					playMaintenance(level, player, SoundEvents.AXE_SCRAPE.value(), false);
				}
				tool.hurtAndBreak(1, player, otherHand);
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	private static void playMaintenance(Level level, Player player, SoundEvent sound, boolean waxOn) {
		level.playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, 1.0f, 1.0f);
		if (level instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(waxOn ? ParticleTypes.WAX_ON : ParticleTypes.SCRAPE,
					player.getX(), player.getY() + 1.0, player.getZ(), 10, 0.4, 0.4, 0.4, 0.5);
		}
	}
}
