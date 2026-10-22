package uk.billysmods;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
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
        fasterFlight = register("key.billys-mods.flight_faster", GLFW.GLFW_KEY_PAGE_UP, category);
        slowerFlight = register("key.billys-mods.flight_slower", GLFW.GLFW_KEY_PAGE_DOWN, category);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
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
