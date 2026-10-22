package uk.billysmods;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FlightTogglePayload(int action) implements CustomPacketPayload {
    public static final Type<FlightTogglePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(BillysMods.MOD_ID, "flight_control"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FlightTogglePayload> CODEC =
            StreamCodec.of(
                    (buffer, payload) -> buffer.writeVarInt(payload.action()),
                    buffer -> new FlightTogglePayload(buffer.readVarInt())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
