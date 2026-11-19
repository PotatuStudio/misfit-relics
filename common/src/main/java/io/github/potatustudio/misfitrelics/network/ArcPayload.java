package io.github.potatustudio.misfitrelics.network;

import io.github.potatustudio.misfitrelics.MisfitRelics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;

/**
 * Servidor → cliente: un arco eléctrico. El cliente lo dibuja durante {@code ticks} (antes eran cientos de paquetes de
 * partículas por arco). Si {@code fromId}/{@code toId} son ≥ 0, el arco sigue a esas entidades; si no, usa las posiciones.
 */
public record ArcPayload(int fromId, int toId, Vec3 from, Vec3 to, int ticks) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<ArcPayload> TYPE = new CustomPacketPayload.Type<>(MisfitRelics.id("arc"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ArcPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, ArcPayload::fromId,
			ByteBufCodecs.VAR_INT, ArcPayload::toId,
			Vec3.STREAM_CODEC, ArcPayload::from,
			Vec3.STREAM_CODEC, ArcPayload::to,
			ByteBufCodecs.VAR_INT, ArcPayload::ticks,
			ArcPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
