package uk.billysmods;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

/** Client keybinds send requests; flight state and movement remain server authoritative. */
public final class ViltrumiteClient implements ClientModInitializer {
    private static KeyMapping flightToggle;
    private static KeyMapping fasterFlight;
    private static KeyMapping slowerFlight;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(
                ResourceLocation.fromNamespaceAndPath(BillysMods.MOD_ID, "flight"));
        flightToggle = register("key.billys-mods.toggle_flight", GLFW.GLFW_KEY_G, category);
        fasterFlight = register("key.billys-mods.flight_faster", GLFW.GLFW_KEY_RIGHT_BRACKET, category);
        slowerFlight = register("key.billys-mods.flight_slower", GLFW.GLFW_KEY_LEFT_BRACKET, category);

        ClientPlayNetworking.registerGlobalReceiver(FlightStatePayload.TYPE,
                (payload, context) -> context.client().execute(() ->
                        FlightClientState.setFlying(payload.entityId(), payload.active())));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> FlightClientState.clear());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> FlightClientState.clear());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            FlightClientState.removeMissing(client.level);
            if (client.level != null) {
                for (Player player : client.level.players()) {
                    if (FlightClientState.isFlying(player.getId())) {
                        // Reset vanilla limb animation after entity ticks and before rendering.
                        player.walkAnimation.stop();
                    }
                }
            }
            if (client.player == null || client.screen != null) {
                return;
            }
            while (flightToggle.consumeClick()) {
                ClientPlayNetworking.send(new FlightTogglePayload(0));
            }
            while (fasterFlight.consumeClick()) {
                ClientPlayNetworking.send(new FlightTogglePayload(1));
            }
            while (slowerFlight.consumeClick()) {
                ClientPlayNetworking.send(new FlightTogglePayload(-1));
            }
        });
    }

    private static KeyMapping register(String translationKey, int key, KeyMapping.Category category) {
        return KeyBindingHelper.registerKeyBinding(new KeyMapping(
                translationKey, InputConstants.Type.KEYSYM, key, category));
    }
}
