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

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(
                ResourceLocation.fromNamespaceAndPath(BillysMods.MOD_ID, "flight"));
        flightToggle = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.billys-mods.toggle_flight",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (flightToggle.consumeClick()) {
                if (client.player != null && client.screen == null) {
                    ClientPlayNetworking.send(new FlightTogglePayload());
                }
            }
        });
    }
}
