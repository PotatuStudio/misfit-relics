package io.github.potatustudio.misfitrelics.stench;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;

/**
 * Estado del Hedor en una entidad.
 *
 * @param source    UUID de quien originó el Hedor (quien lanzó la poción o disparó); vacío si es natural.
 * @param canSpread si esta entidad puede contagiar a otras.
 * @param carrier   portador: no sufre el daño de su propio Hedor, pero contagia.
 */
public record StenchData(Optional<UUID> source, boolean canSpread, boolean carrier) {
	public static final StenchData NONE = new StenchData(Optional.empty(), true, false);

	public static final Codec<StenchData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			UUIDUtil.CODEC.optionalFieldOf("source").forGetter(StenchData::source),
			Codec.BOOL.optionalFieldOf("can_spread", true).forGetter(StenchData::canSpread),
			Codec.BOOL.optionalFieldOf("carrier", false).forGetter(StenchData::carrier)
	).apply(instance, StenchData::new));
}
