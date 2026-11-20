package io.github.potatustudio.misfitrelics.combat;

import io.github.potatustudio.misfitrelics.relic.AccessoryHooks;
import static io.github.potatustudio.misfitrelics.relic.TooltipNumbers.fmt;
import io.github.potatustudio.misfitrelics.registry.ModLoot;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/** Reliquia de combate (ver {@link CombatRelics}). Cada tipo tiene su tooltip, su sonido y, si corresponde, atributos. */
public class CombatItem extends Item implements AccessoryHooks {
	/** Anillo del berserker: vida máxima −90 % (te deja con 1 corazón) y daño +200 %. */
	public static final double BERSERKER_HEALTH = -0.9;
	public static final double BERSERKER_DAMAGE = 2.0;

	public enum Kind {
		ELECTRIC_GAUNTLET("electric_gauntlet"),
		CHARGED_CREEPER_HEART("charged_creeper_heart"),
		FOREST_QUIVER("forest_quiver"),
		WATCHER_QUIVER("watcher_quiver"),
		NAUTILUS_SHELL("nautilus_shell"),
		BERSERKER_RING("berserker_ring");

		final String id;

		Kind(String id) {
			this.id = id;
		}
	}

	private final Kind kind;

	public CombatItem(Kind kind, Properties properties) {
		super(properties);
		this.kind = kind;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
			Consumer<Component> tooltip, TooltipFlag flag) {
		Object[] args = switch (kind) {
			case ELECTRIC_GAUNTLET -> new Object[] {CombatRelics.MAX_CHARGE,
					fmt(CombatRelics.SHOCK_BASE + CombatRelics.SHOCK_PER_CHARGE), fmt(CombatRelics.SHOCK_PER_CHARGE),
					fmt(CombatRelics.SHOCK_BASE + CombatRelics.SHOCK_PER_CHARGE * CombatRelics.MAX_CHARGE),
					CombatRelics.CHAIN_JUMPS, Math.round(CombatRelics.CHAIN_FALLOFF * 100), CombatRelics.CHARGE_TIMEOUT / 20};
			case CHARGED_CREEPER_HEART -> new Object[] {Math.round(CombatRelics.HEART_CHANCE * 100),
					CombatRelics.HEART_STUN / 20, CombatRelics.HEART_COOLDOWN / 20};
			case FOREST_QUIVER -> new Object[] {CombatRelics.ENTANGLE_TICKS / 20};
			case BERSERKER_RING -> new Object[] {Math.round(-BERSERKER_HEALTH * 100), Math.round(BERSERKER_DAMAGE * 100)};
			default -> new Object[0];
		};
		for (int i = 1; Language.getInstance().has("item.misfitrelics." + kind.id + ".tooltip." + i); i++) {
			tooltip.accept(Component.translatable("item.misfitrelics." + kind.id + ".tooltip." + i, args)
					.withStyle(i == 1 ? ChatFormatting.GOLD : ChatFormatting.GRAY));
		}
		ModLoot.appendWhere(this, tooltip);
	}

	/** Atributos mientras se lleva puesto (Trinkets o Curios los aplican y los quitan). */
	@Override
	public void forEachModifier(ItemStack stack, LivingEntity entity, Identifier id,
			BiConsumer<Holder<Attribute>, AttributeModifier> consumer) {
		switch (kind) {
			case NAUTILUS_SHELL ->
				// Caminar bajo el agua sin frenarse; nadando, la gracia del delfín (CombatRelics).
				consumer.accept(Attributes.WATER_MOVEMENT_EFFICIENCY, new AttributeModifier(id, 1.0, AttributeModifier.Operation.ADD_VALUE));
			case BERSERKER_RING -> {
				consumer.accept(Attributes.MAX_HEALTH,
						new AttributeModifier(id, BERSERKER_HEALTH, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
				consumer.accept(Attributes.ATTACK_DAMAGE,
						new AttributeModifier(id, BERSERKER_DAMAGE, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
			}
			default -> {
			}
		}
	}

	@Override
	public void onEquip(ItemStack stack, LivingEntity entity) {
		if (entity.level() instanceof ServerLevel level) {
			SoundEvent sound = switch (kind) {
				case ELECTRIC_GAUNTLET -> SoundEvents.COPPER_BULB_TURN_ON;
				case CHARGED_CREEPER_HEART -> SoundEvents.CREEPER_PRIMED;
				case FOREST_QUIVER -> SoundEvents.SWEET_BERRY_BUSH_PLACE;
				case WATCHER_QUIVER -> SoundEvents.ENDER_EYE_DEATH;
				case NAUTILUS_SHELL -> SoundEvents.CONDUIT_ACTIVATE;
				case BERSERKER_RING -> SoundEvents.PIGLIN_BRUTE_ANGRY;
			};
			level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound, SoundSource.PLAYERS, 1.0f, 1.0f);
		}
	}
}
