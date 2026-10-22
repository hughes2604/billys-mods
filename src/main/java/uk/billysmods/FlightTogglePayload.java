package uk.billysmods;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FlightTogglePayload(int action, float forward, float strafe, boolean jump, boolean shift)
        implements CustomPacketPayload {
    public FlightTogglePayload(int action) {
        this(action, 0.0F, 0.0F, false, false);
    }

    public static final Type<FlightTogglePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(BillysMods.MOD_ID, "flight_control"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FlightTogglePayload> CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeVarInt(payload.action());
                buffer.writeFloat(payload.forward());
                buffer.writeFloat(payload.strafe());
                buffer.writeBoolean(payload.jump());
                buffer.writeBoolean(payload.shift());
            },
            buffer -> new FlightTogglePayload(buffer.readVarInt(), buffer.readFloat(), buffer.readFloat(),
                    buffer.readBoolean(), buffer.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
