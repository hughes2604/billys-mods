package uk.billysmods;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

/** Client keybinds send requests; flight state and movement remain server authoritative. */
public final class ViltrumiteClient implements ClientModInitializer {
    private static KeyMapping flightToggle;
    private static KeyMapping flightBoost;
    private static KeyMapping lowerBoostLevel;
    private static KeyMapping raiseBoostLevel;
    private static boolean boostWasDown;

    @Override
    public void onInitializeClient() {
        String category = "category.billys-mods.flight";
        flightToggle = register("key.billys-mods.toggle_flight", GLFW.GLFW_KEY_G, category);
        flightBoost = register("key.billys-mods.flight_boost", GLFW.GLFW_KEY_LEFT_CONTROL, category);
        lowerBoostLevel = register("key.billys-mods.lower_boost_level", GLFW.GLFW_KEY_LEFT_BRACKET, category);
        raiseBoostLevel = register("key.billys-mods.raise_boost_level", GLFW.GLFW_KEY_RIGHT_BRACKET, category);

        ClientPlayNetworking.registerGlobalReceiver(FlightStatePayload.TYPE,
                (payload, context) -> context.client().execute(() -> {
                    boolean wasBoosting = FlightClientState.isBoosting(payload.entityId());
                    FlightClientState.setFlight(payload.entityId(), payload.active(), payload.boosting());
                    if (payload.boosting() && !wasBoosting && context.client().player != null
                            && context.client().player.getId() == payload.entityId()) {
                        context.client().getSoundManager().play(SimpleSoundInstance.forLocalAmbience(
                                SoundEvents.TRIDENT_RIPTIDE_3.value(), 1.25F, 0.85F));
                    }
                }));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            FlightClientState.clear();
            boostWasDown = false;
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            FlightClientState.clear();
            boostWasDown = false;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            FlightClientState.removeMissing(client.level);
            if (client.level != null) {
                for (Player player : client.level.players()) {
                    if (FlightClientState.isFlying(player.getId())) {
                        // Reset vanilla limb animation after entity ticks and before rendering.
                    }
                }
            }
            if (client.player == null || client.screen != null) {
                if (boostWasDown) {
                    ClientPlayNetworking.send(new FlightTogglePayload(2));
                    boostWasDown = false;
                }
                return;
            }
            while (flightToggle.consumeClick()) {
                ClientPlayNetworking.send(new FlightTogglePayload(0));
            }
            while (lowerBoostLevel.consumeClick()) {
                ClientPlayNetworking.send(new FlightTogglePayload(4));
            }
            while (raiseBoostLevel.consumeClick()) {
                ClientPlayNetworking.send(new FlightTogglePayload(3));
            }
            boolean boostDown = FlightClientState.isFlying(client.player.getId()) && flightBoost.isDown();
            if (boostDown != boostWasDown) {
                ClientPlayNetworking.send(new FlightTogglePayload(boostDown ? 1 : 2));
                boostWasDown = boostDown;
            }
        });
    }

    private static KeyMapping register(String translationKey, int key, String category) {
        return KeyBindingHelper.registerKeyBinding(new KeyMapping(
                translationKey, InputConstants.Type.KEYSYM, key, category));
    }
}
