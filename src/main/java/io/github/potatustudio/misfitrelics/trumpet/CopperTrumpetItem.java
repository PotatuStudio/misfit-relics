package io.github.potatustudio.misfitrelics.trumpet;

import io.github.potatustudio.misfitrelics.registry.ModEffects;
import io.github.potatustudio.misfitrelics.registry.ModGameRules;
import io.github.potatustudio.misfitrelics.registry.ModItems;
import io.github.potatustudio.misfitrelics.util.Allies;
import io.github.potatustudio.misfitrelics.util.CopperAging;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Trompeta de cobre: arma cuerpo a cuerpo e instrumento.
 * <ul>
 *   <li>Golpe: 6 de daño. Pasiva: cada golpe aturde al objetivo (si ya está aturdido, no se renueva).</li>
 *   <li>Eco: si el objetivo queda contra una pared, el aturdimiento dura un 50 % más; bajo techo, un 25 % más.</li>
 *   <li>Clic derecho: toca una melodía al azar. Solo música, no aturde.</li>
 *   <li>Oxidación: cada golpe tiene una probabilidad de pasar a la siguiente etapa, salvo que esté encerada.</li>
 *   <li>Agachado con panal en la otra mano: la encera. Agachado con hacha: quita la cera o una etapa de óxido.</li>
 * </ul>
 */
public class CopperTrumpetItem extends Item {
	public static final int DURABILITY = 200;
	/** Daño total del golpe (1 de la mano + 5 de la trompeta). */
	public static final double ATTACK_DAMAGE = 6.0;
	/** Velocidad de ataque como la de una espada (1,6 golpes por segundo). */
	public static final double ATTACK_SPEED = -2.4;
	/** Probabilidad de oxidarse en cada golpe (3 %). */
	public static final float OXIDATION_CHANCE = 0.03f;
	public static final float WALL_ECHO_MULTIPLIER = 1.5f;
	public static final float ROOF_ECHO_MULTIPLIER = 1.25f;
	/** Pausa entre melodías, además de lo que dura la melodía. */
	private static final int SONG_COOLDOWN = 10;

	private final TrumpetStage stage;

	public CopperTrumpetItem(TrumpetStage stage, Properties properties) {
		super(properties);
		this.stage = stage;
	}

	/** Atributos de arma: aparecen en el tooltip vanilla ("En la mano principal: 6 de daño por ataque"). */
	public static ItemAttributeModifiers attributes() {
		return ItemAttributeModifiers.builder()
				.add(Attributes.ATTACK_DAMAGE,
						new AttributeModifier(BASE_ATTACK_DAMAGE_ID, ATTACK_DAMAGE - 1.0, AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.MAINHAND)
				.add(Attributes.ATTACK_SPEED,
						new AttributeModifier(BASE_ATTACK_SPEED_ID, ATTACK_SPEED, AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.MAINHAND)
				.build();
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
		if (level instanceof ServerLevel serverLevel) {
			TrumpetSongs.Song song = TrumpetSongs.random(player.getRandom());
			TrumpetSongs.play(serverLevel, player, song, stage.sound);
			player.getCooldowns().addCooldown(trumpet, song.length() + SONG_COOLDOWN);
			player.sendOverlayMessage(Component.translatable("trumpet.misfitrelics.song." + song.key())
					.withStyle(ChatFormatting.GOLD));
		}
		player.awardStat(Stats.ITEM_USED.get(this));
		return InteractionResult.SUCCESS;
	}

	/** Pasiva: cada golpe aturde. El desgaste por golpe lo pone el componente {@code weapon}. */
	@Override
	public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (!(attacker.level() instanceof ServerLevel level) || !target.isAlive()) {
			return;
		}
		boolean friendlyFire = level.getGameRules().get(ModGameRules.TRUMPET_FRIENDLY_FIRE);
		if (!target.hasEffect(ModEffects.STUNNED) && (friendlyFire || !Allies.isAlly(level, target, attacker.getUUID()))) {
			stun(level, attacker, target);
		}
	}

	/** Después del golpe (y del desgaste): puede oxidarse. */
	@Override
	public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		super.postHurtEnemy(stack, target, attacker);
		if (attacker instanceof Player player && !stack.isEmpty()
				&& player.getItemInHand(InteractionHand.MAIN_HAND) == stack) {
			maybeOxidize(player, InteractionHand.MAIN_HAND, stack);
		}
	}

	private void stun(ServerLevel level, LivingEntity attacker, LivingEntity target) {
		Vec3 direction = target.position().subtract(attacker.position()).multiply(1, 0, 1);
		direction = direction.lengthSqr() < 1.0E-4 ? attacker.getLookAngle() : direction.normalize();
		float duration = stage.stunTicks;
		boolean wallEcho = isAgainstWall(level, target, direction);
		if (wallEcho) {
			duration *= WALL_ECHO_MULTIPLIER;
		}
		if (!level.canSeeSky(target.blockPosition())) {
			duration *= ROOF_ECHO_MULTIPLIER;
		}
		target.addEffect(new MobEffectInstance(ModEffects.STUNNED, Math.round(duration)), attacker);
		level.playSound(null, target.getX(), target.getEyeY(), target.getZ(), stage.sound.value(),
				attacker.getSoundSource(), 1.5f, wallEcho ? 0.5f : 0.7f);

		Vec3 center = target.getBoundingBox().getCenter();
		level.sendParticles(wallEcho ? ParticleTypes.ELECTRIC_SPARK : ParticleTypes.CRIT,
				center.x, center.y + target.getBbHeight() * 0.3, center.z, wallEcho ? 12 : 6, 0.3, 0.3, 0.3, 0.05);
		level.sendParticles(ParticleTypes.NOTE, center.x, target.getY() + target.getBbHeight() + 0.3, center.z,
				3, 0.3, 0.1, 0.3, 0.0);
	}

	/** Hay un bloque sólido justo detrás del objetivo, en la dirección del golpe. */
	private static boolean isAgainstWall(ServerLevel level, LivingEntity target, Vec3 direction) {
		Vec3 behind = target.getBoundingBox().getCenter().add(direction.scale(target.getBbWidth() / 2.0 + 0.6));
		return level.getBlockState(BlockPos.containing(behind)).isSolidRender();
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
			Consumer<Component> tooltip, TooltipFlag flag) {
		tooltip.accept(Component.translatable("item.misfitrelics.copper_trumpet.tooltip.stun",
				stage.stunTicks / 20.0).withStyle(ChatFormatting.GOLD));
		tooltip.accept(Component.translatable("item.misfitrelics.copper_trumpet.tooltip.echo",
				Math.round((WALL_ECHO_MULTIPLIER - 1) * 100), Math.round((ROOF_ECHO_MULTIPLIER - 1) * 100))
				.withStyle(ChatFormatting.GRAY));
		tooltip.accept(Component.translatable("item.misfitrelics.copper_trumpet.tooltip.play").withStyle(ChatFormatting.GRAY));
		if (stack.has(DataComponents.WAXED)) {
			tooltip.accept(Component.translatable("item.misfitrelics.copper_trumpet.tooltip.waxed").withStyle(ChatFormatting.YELLOW));
		} else if (stage != TrumpetStage.OXIDIZED) {
			tooltip.accept(Component.translatable("item.misfitrelics.copper_trumpet.tooltip.oxidizes",
					Math.round(OXIDATION_CHANCE * 100)).withStyle(ChatFormatting.DARK_GREEN));
		}
		tooltip.accept(Component.translatable("item.misfitrelics.copper_trumpet.tooltip.maintenance").withStyle(ChatFormatting.DARK_GRAY));
	}

	private void maybeOxidize(Player player, InteractionHand hand, ItemStack trumpet) {
		Item next = stage == TrumpetStage.OXIDIZED ? null : ModItems.trumpetFor(stage.next());
		CopperAging.maybeOxidize(player, hand, trumpet, next, OXIDATION_CHANCE);
	}

	/** Mantenimiento agachado: panal para encerar, hacha para quitar cera u óxido. */
	private InteractionResult tryMaintenance(Level level, Player player, InteractionHand hand, ItemStack trumpet) {
		Item previous = stage == TrumpetStage.COPPER ? null : ModItems.trumpetFor(stage.previous());
		return CopperAging.tryMaintenance(level, player, hand, trumpet, previous);
	}
}
