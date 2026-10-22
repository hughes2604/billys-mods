package uk.billysmods;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FlightStatePayload(int entityId, boolean active, boolean boosting) implements CustomPacketPayload {
    public static final Type<FlightStatePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(BillysMods.MOD_ID, "flight_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FlightStatePayload> CODEC =
            StreamCodec.of(
                    (buffer, payload) -> {
                        buffer.writeVarInt(payload.entityId());
                        buffer.writeBoolean(payload.active());
                        buffer.writeBoolean(payload.boosting());
                    },
                    buffer -> new FlightStatePayload(buffer.readVarInt(), buffer.readBoolean(), buffer.readBoolean())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
