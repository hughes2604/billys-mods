package uk.billysmods;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FlightTogglePayload() implements CustomPacketPayload {
    public static final Type<FlightTogglePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(BillysMods.MOD_ID, "toggle_flight"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FlightTogglePayload> CODEC =
            StreamCodec.unit(new FlightTogglePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
