package io.github.potatustudio.misfitrelics.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.potatustudio.misfitrelics.luck.LuckyRabbitFoot;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Deja que la pata de la suerte cambie el botín ya tirado de cualquier tabla (bloques, mobs, cofres, pesca), en ambos
 * loaders. Es lo mismo que hace el evento {@code LootTableEvents.MODIFY_DROPS} de Fabric API, que Architectury no tiene.
 * En NeoForge corre antes de los modificadores globales de botín.
 */
@Mixin(LootTable.class)
public abstract class LootTableMixin {
	/** La tabla con su id (una tabla no conoce su id: se busca una vez en el registro del servidor). */
	@Unique
	private @Nullable Holder<LootTable> misfitrelics$holder;

	// En vanilla (Fabric) la tirada está en getRandomItems(contexto, consumidor); NeoForge la mueve a la versión que
	// devuelve la lista (para aplicar después sus modificadores globales). Se apunta a las dos: en cada loader calza una.
	@WrapOperation(method = {
			"getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V",
			"getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;"},
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V"))
	private void misfitrelics$modifyDrops(LootTable table, LootContext context, Consumer<ItemStack> output, Operation<Void> original) {
		List<ItemStack> drops = new ArrayList<>();
		original.call(table, context, (Consumer<ItemStack>) drops::add);
		LuckyRabbitFoot.onDrops(misfitrelics$holder(context), context, drops);
		drops.forEach(output);
	}

	@Unique
	private Holder<LootTable> misfitrelics$holder(LootContext context) {
		if (misfitrelics$holder == null) {
			LootTable self = (LootTable) (Object) this;
			misfitrelics$holder = context.getLevel().getServer().reloadableRegistries().lookup()
					.lookupOrThrow(Registries.LOOT_TABLE).listElements()
					.filter(entry -> entry.value() == self)
					.<Holder<LootTable>>map(entry -> entry)
					.findFirst()
					.orElseGet(() -> Holder.direct(self));
		}
		return misfitrelics$holder;
	}
}
