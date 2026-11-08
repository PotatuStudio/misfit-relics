package io.github.potatustudio.misfitrelics.network;

import io.github.potatustudio.misfitrelics.MisfitRelics;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Servidor → cliente: resultado de la ecolocación. Solo lo recibe quien usó la tecla, y su cliente dibuja el contorno
 * de esas entidades; los demás jugadores no ven nada.
 *
 * @param entityIds entidades detectadas
 * @param ticks     cuánto dura el contorno
 */
public record EcholocationPulsePayload(List<Integer> entityIds, int ticks) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<EcholocationPulsePayload> TYPE =
			new CustomPacketPayload.Type<>(MisfitRelics.id("echolocation_pulse"));
	public static final StreamCodec<RegistryFriendlyByteBuf, EcholocationPulsePayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), EcholocationPulsePayload::entityIds,
			ByteBufCodecs.VAR_INT, EcholocationPulsePayload::ticks,
			EcholocationPulsePayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
