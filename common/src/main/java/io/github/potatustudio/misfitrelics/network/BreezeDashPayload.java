package io.github.potatustudio.misfitrelics.network;

import io.github.potatustudio.misfitrelics.MisfitRelics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Cliente → servidor: el jugador usó la ráfaga del breeze en una botella. No lleva datos. */
public record BreezeDashPayload() implements CustomPacketPayload {
	public static final BreezeDashPayload INSTANCE = new BreezeDashPayload();
	public static final CustomPacketPayload.Type<BreezeDashPayload> TYPE = new CustomPacketPayload.Type<>(MisfitRelics.id("breeze_dash"));
	public static final StreamCodec<RegistryFriendlyByteBuf, BreezeDashPayload> CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
