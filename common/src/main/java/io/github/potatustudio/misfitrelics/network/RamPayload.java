package io.github.potatustudio.misfitrelics.network;

import io.github.potatustudio.misfitrelics.MisfitRelics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Cliente → servidor: el jugador pulsó la tecla de embestir (cuernos del carnero). No lleva datos. */
public record RamPayload() implements CustomPacketPayload {
	public static final RamPayload INSTANCE = new RamPayload();
	public static final CustomPacketPayload.Type<RamPayload> TYPE = new CustomPacketPayload.Type<>(MisfitRelics.id("ram"));
	public static final StreamCodec<RegistryFriendlyByteBuf, RamPayload> CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
