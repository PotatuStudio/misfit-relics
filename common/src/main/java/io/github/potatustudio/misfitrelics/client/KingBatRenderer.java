package io.github.potatustudio.misfitrelics.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.potatustudio.misfitrelics.MisfitRelics;
import net.minecraft.client.model.ambient.BatModel;
import net.minecraft.client.renderer.entity.BatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.BatRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/**
 * Rey murciélago: el murciélago vanilla más grande y gordo, teñido de un tono oscuro rojizo y con ojos que brillan
 * en la oscuridad (capa propia con solo los píxeles de los ojos).
 */
public class KingBatRenderer extends BatRenderer {
	private static final float SCALE = 1.7f;
	/** Más ancho que alto: gordito. */
	private static final float GIRTH = 1.15f;
	private static final int TINT = 0xFFFF8F80;
	private static final RenderType EYES = RenderTypes.eyes(MisfitRelics.id("textures/entity/king_bat_eyes.png"));

	public KingBatRenderer(EntityRendererProvider.Context context) {
		super(context);
		addLayer(new EyesLayer<BatRenderState, BatModel>(this) {
			@Override
			public RenderType renderType() {
				return EYES;
			}
		});
	}

	@Override
	protected void scale(BatRenderState state, PoseStack poseStack) {
		super.scale(state, poseStack);
		poseStack.scale(SCALE * GIRTH, SCALE, SCALE * GIRTH);
	}

	@Override
	protected int getModelTint(BatRenderState state) {
		return TINT;
	}
}
