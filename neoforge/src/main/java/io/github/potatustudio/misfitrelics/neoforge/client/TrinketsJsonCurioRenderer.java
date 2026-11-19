package io.github.potatustudio.misfitrelics.neoforge.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import com.mojang.serialization.JsonOps;
import io.github.potatustudio.misfitrelics.MisfitRelics;
import io.github.potatustudio.misfitrelics.registry.ModItems;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/**
 * Dibuja una reliquia puesta en Curios leyendo el mismo {@code assets/misfitrelics/trinkets/<reliquia>.json} que usa
 * Trinkets en Fabric, con la misma cuenta que Trinkets 4.2.1 ({@code TrinketRenderLayer.submitAttached}), para que se vea
 * igual en los dos loaders con una sola fuente:
 * <ol>
 *   <li>pegada a la parte del cuerpo ({@code model_part}: body, head o la mano principal u otra);</li>
 *   <li>en el centro de los cubos propios de esa parte, desplazada {@code offset} medios tamaños (Y hacia arriba, Z hacia
 *       el frente);</li>
 *   <li>con {@code scale(1, -1, -1)} y después la {@code transformation} del JSON.</li>
 * </ol>
 * Los elementos {@code item} dibujan el ítem en su contexto ({@code none} por defecto); los {@code model} dibujan el ítem en
 * el contexto {@code none}, cuya definición elige el modelo puesto. {@code minecraft:if_slot} elige la mano: el espacio
 * de manos de Curios es uno solo (los guantes se ven en las dos), y en los anillos el primer espacio es la mano principal.
 */
public final class TrinketsJsonCurioRenderer implements ICurioRenderer {
	private record Element(String type, String modelPart, Vector3f offset, Optional<Transformation> transformation,
			ItemDisplayContext displayContext, @Nullable String slot) {
	}

	private final Identifier itemId;
	private final ItemStackRenderState item = new ItemStackRenderState();
	private @Nullable List<Element> elements;

	public TrinketsJsonCurioRenderer(Identifier itemId) {
		this.itemId = itemId;
	}

	@Override
	public <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void render(ItemStack stack, SlotContext slotContext,
			PoseStack poseStack, SubmitNodeCollector collector, int light, S renderState, RenderLayerParent<S, M> parent,
			EntityRendererProvider.Context context, float yRotation, float xRotation) {
		if (!(parent.getModel() instanceof HumanoidModel<?> model)) {
			return;
		}
		LivingEntity entity = slotContext.entity();
		for (Element element : elements()) {
			if (element.slot() != null && !slotMatches(element.slot(), slotContext)) {
				continue;
			}
			ModelPart part = part(model, element.modelPart(), entity.getMainArm());
			if (part == null) {
				continue;
			}
			ItemDisplayContext display = element.type().equals("model") ? ItemDisplayContext.NONE : element.displayContext();
			Minecraft.getInstance().getItemModelResolver().updateForTopItem(item, stack, display, entity.level(), entity, entity.getId());
			draw(model, part, element, poseStack, collector, light);
		}
	}

	/** En Curios, "hand/..." es el primer espacio del tipo y "offhand/..." el segundo; las manos (un espacio) van en ambas. */
	private static boolean slotMatches(String slot, SlotContext slotContext) {
		if (slotContext.identifier().equals("hands")) {
			return true;
		}
		return slot.startsWith("offhand/") ? slotContext.index() == 1 : slotContext.index() == 0;
	}

	private static @Nullable ModelPart part(HumanoidModel<?> model, String name, HumanoidArm mainArm) {
		HumanoidArm arm = switch (name) {
			case ":main_hand", ":mainhand", ":hand" -> mainArm;
			case ":off_hand", ":offhand" -> mainArm.getOpposite();
			default -> null;
		};
		String resolved = arm == null ? name : arm == HumanoidArm.RIGHT ? "right_arm" : "left_arm";
		return switch (resolved) {
			case "head" -> model.head;
			case "body" -> model.body;
			case "right_arm" -> model.rightArm;
			case "left_arm" -> model.leftArm;
			case "right_leg" -> model.rightLeg;
			case "left_leg" -> model.leftLeg;
			default -> null;
		};
	}

	private void draw(HumanoidModel<?> model, ModelPart part, Element element, PoseStack poseStack, SubmitNodeCollector collector, int light) {
		float[] box = ownCubeBounds(part);
		float cx = (box[0] + box[3]) / 2f / 16f;
		float cy = (box[1] + box[4]) / 2f / 16f;
		float cz = (box[2] + box[5]) / 2f / 16f;
		float lx = (box[3] - box[0]) / 2f / 16f;
		float ly = (box[4] - box[1]) / 2f / 16f;
		float lz = (box[5] - box[2]) / 2f / 16f;
		poseStack.pushPose();
		model.root().translateAndRotate(poseStack);
		part.translateAndRotate(poseStack);
		poseStack.translate(cx + lx * element.offset().x(), cy - ly * element.offset().y(), cz - lz * element.offset().z());
		poseStack.scale(1f, -1f, -1f);
		element.transformation().ifPresent(t -> poseStack.mulPose(t.getMatrix()));
		item.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
		poseStack.popPose();
	}

	/** Caja (en píxeles, espacio local de la parte) de los cubos propios de la parte, sin las capas de la skin. */
	private static float[] ownCubeBounds(ModelPart part) {
		float[] box = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
		part.visit(new PoseStack(), (pose, path, index, cube) -> {
			if (!path.isEmpty()) {
				return;
			}
			box[0] = Math.min(box[0], cube.minX);
			box[1] = Math.min(box[1], cube.minY);
			box[2] = Math.min(box[2], cube.minZ);
			box[3] = Math.max(box[3], cube.maxX);
			box[4] = Math.max(box[4], cube.maxY);
			box[5] = Math.max(box[5], cube.maxZ);
		});
		return box[0] == Float.MAX_VALUE ? new float[6] : box;
	}

	/** Lee el JSON la primera vez que se dibuja (los recursos ya están cargados). */
	private List<Element> elements() {
		if (elements == null) {
			elements = new ArrayList<>();
			Identifier file = MisfitRelics.id("trinkets/" + itemId.getPath() + ".json");
			try (Reader reader = Minecraft.getInstance().getResourceManager().openAsReader(file)) {
				for (JsonElement render : JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("render")) {
					read(render.getAsJsonObject(), null, elements);
				}
			} catch (Exception e) {
				MisfitRelics.LOGGER.error("No se pudo leer {} para dibujar la reliquia en Curios", file, e);
			}
		}
		return elements;
	}

	private static void read(JsonObject json, @Nullable String slot, List<Element> out) {
		String type = json.get("type").getAsString().replace("minecraft:", "");
		if (type.equals("if_slot")) {
			read(json.getAsJsonObject("then"), json.get("slot").getAsString(), out);
			return;
		}
		Vector3f offset = new Vector3f();
		if (json.has("offset")) {
			JsonArray o = json.getAsJsonArray("offset");
			offset.set(o.get(0).getAsFloat(), o.get(1).getAsFloat(), o.get(2).getAsFloat());
		}
		Optional<Transformation> transformation = json.has("transformation")
				? Transformation.EXTENDED_CODEC.parse(JsonOps.INSTANCE, json.get("transformation")).result()
				: Optional.empty();
		ItemDisplayContext display = json.has("display_context")
				? ItemDisplayContext.CODEC.parse(JsonOps.INSTANCE, json.get("display_context")).result().orElse(ItemDisplayContext.NONE)
				: ItemDisplayContext.NONE;
		out.add(new Element(type, json.get("model_part").getAsString(), offset, transformation, display, slot));
	}

	/** Un renderizador por reliquia de accesorio; si no tiene JSON de Trinkets, no dibuja nada (y lo avisa una vez). */
	public static void registerAll() {
		for (var item : ModItems.accessoryItems()) {
			Identifier id = BuiltInRegistries.ITEM.getKey(item);
			ICurioRenderer.register(item, () -> new TrinketsJsonCurioRenderer(id));
		}
	}
}
