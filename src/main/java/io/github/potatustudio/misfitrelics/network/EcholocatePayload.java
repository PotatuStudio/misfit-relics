package io.github.potatustudio.misfitrelics.network;

import io.github.potatustudio.misfitrelics.MisfitRelics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Cliente → servidor: el jugador pulsó la tecla de ecolocación. No lleva datos. */
public record EcholocatePayload() implements CustomPacketPayload {
	public static final EcholocatePayload INSTANCE = new EcholocatePayload();
	public static final CustomPacketPayload.Type<EcholocatePayload> TYPE =
			new CustomPacketPayload.Type<>(MisfitRelics.id("echolocate"));
	public static final StreamCodec<RegistryFriendlyByteBuf, EcholocatePayload> CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
