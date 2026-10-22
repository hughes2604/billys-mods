package uk.billysmods;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class ViltrumiteGenome {
    private static final ResourceLocation ATTACK_DAMAGE_ID = id("genome_strength");
    private static final ResourceLocation ATTACK_KNOCKBACK_ID = id("genome_knockback");
    private static final ResourceLocation ARMOR_ID = id("genome_durability");
    private static final ResourceLocation TOUGHNESS_ID = id("genome_toughness");
    private static final ResourceLocation MAX_HEALTH_ID = id("genome_vitality");
    private static final Map<UUID, Integer> FLIGHT_SPEED = new HashMap<>();
    private static final double[] FLIGHT_SPEEDS = {0.0D, 0.36D, 0.95D, 2.40D};

    private ViltrumiteGenome() { }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(BillysMods.MOD_ID, path);
    }

    public static boolean hasGenome(Player player) {
        var inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).is(BillysMods.VILTRUMITE_GENOME)) {
                return true;
            }
        }
        return false;
    }

    public static void tickServer(ServerPlayer player) {
        boolean empowered = hasGenome(player);
        setModifier(player, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_ID, 49.0D, empowered);
        setModifier(player, Attributes.ATTACK_KNOCKBACK, ATTACK_KNOCKBACK_ID, 2.5D, empowered);
        setModifier(player, Attributes.ARMOR, ARMOR_ID, 20.0D, empowered);
        setModifier(player, Attributes.ARMOR_TOUGHNESS, TOUGHNESS_ID, 12.0D, empowered);
        setModifier(player, Attributes.MAX_HEALTH, MAX_HEALTH_ID, 20.0D, empowered);
        if (empowered) {
            MobEffectInstance resistance = player.getEffect(MobEffects.RESISTANCE);
            if (resistance == null || resistance.getAmplifier() < 3 || resistance.getDuration() <= 20) {
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 40, 3, false, false, false));
            }
        }
        tickFlight(player);
    }

    private static void setModifier(
            Player player,
            Holder<Attribute> attribute,
            ResourceLocation id,
            double amount,
            boolean enabled
    ) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        if (enabled) {
            if (instance.getModifier(id) == null) {
                instance.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
            }
        } else {
            instance.removeModifier(id);
        }
    }

    public static void handleFlightControl(ServerPlayer player, int action) {
        if (!hasGenome(player)) {
            return;
        }
        UUID playerId = player.getUUID();
        Integer current = FLIGHT_SPEED.get(playerId);
        if (action == 0) {
            if (current != null) {
                stopFlight(player, playerId);
                player.displayClientMessage(Component.literal("VILTRUMITE FLIGHT OFF")
                        .withStyle(ChatFormatting.GRAY), true);
            } else {
                FLIGHT_SPEED.put(playerId, 1);
                player.setNoGravity(true);
                player.stopFallFlying();
                publishFlightState(player, true);
                showFlightSpeed(player, 1);
            }
            return;
        }
        if (current == null) {
            return;
        }
        int next = Math.max(1, Math.min(3, current + Integer.signum(action)));
        if (next != current) {
            FLIGHT_SPEED.put(playerId, next);
            player.setNoGravity(true);
            showFlightSpeed(player, next);
        }
    }

    private static void showFlightSpeed(ServerPlayer player, int speed) {
        String label = switch (speed) {
            case 1 -> "HOVER";
            case 2 -> "CRUISE";
            default -> "FULL THRUST";
        };
        player.displayClientMessage(Component.literal("FLIGHT SPEED " + speed + "/3  •  " + label)
                .withStyle(ChatFormatting.AQUA), true);
    }

    public static void stopFlightOnDisconnect(ServerPlayer player) {
        if (FLIGHT_SPEED.remove(player.getUUID()) != null) {
            player.setNoGravity(false);
            player.stopFallFlying();
        }
    }

    public static void syncFlightState(ServerPlayer viewer) {
        for (ServerPlayer subject : viewer.getServer().getPlayerList().getPlayers()) {
            if (FLIGHT_SPEED.containsKey(subject.getUUID()) && subject.level() == viewer.level()) {
                ServerPlayNetworking.send(viewer, new FlightStatePayload(subject.getId(), true));
            }
        }
    }

    public static void syncTrackedPlayer(ServerPlayer subject, ServerPlayer viewer) {
        if (FLIGHT_SPEED.containsKey(subject.getUUID())) {
            ServerPlayNetworking.send(viewer, new FlightStatePayload(subject.getId(), true));
        }
    }

    private static void publishFlightState(ServerPlayer subject, boolean active) {
        for (ServerPlayer viewer : subject.getServer().getPlayerList().getPlayers()) {
            if (viewer.level() == subject.level()) {
                ServerPlayNetworking.send(viewer, new FlightStatePayload(subject.getId(), active));
            }
        }
    }

    private static void stopFlight(ServerPlayer player, UUID playerId) {
        FLIGHT_SPEED.remove(playerId);
        publishFlightState(player, false);
        player.setNoGravity(false);
        player.stopFallFlying();
        player.setDeltaMovement(player.getDeltaMovement().scale(0.2D));
    }

    private static void tickFlight(ServerPlayer player) {
        UUID playerId = player.getUUID();
        Integer stage = FLIGHT_SPEED.get(playerId);
        if (!hasGenome(player)) {
            if (stage != null) {
                stopFlight(player, playerId);
            }
            return;
        }
        if (stage == null) {
            return;
        }

        player.setNoGravity(true);
        Input input = player.getLastClientInput();
        Vec3 look = player.getLookAngle().normalize();
        float yawRadians = player.getYRot() * ((float) Math.PI / 180.0F);
        // Negate the yaw perpendicular: at yaw zero, Minecraft faces +Z and its right is -X.
        Vec3 right = new Vec3(-Math.cos(yawRadians), 0.0D, -Math.sin(yawRadians));
        double forwardInput = (input.forward() ? 1.0D : 0.0D) - (input.backward() ? 1.0D : 0.0D);
        double strafeInput = (input.right() ? 1.0D : 0.0D) - (input.left() ? 1.0D : 0.0D);
        double verticalInput = (input.jump() ? 1.0D : 0.0D) - (input.shift() ? 1.0D : 0.0D);

        // Forward/back follow the full view vector, including pitch, like pointing and
        // throttling an aircraft. Jump/sneak provide direct vertical thrust.
        Vec3 direction = look.scale(forwardInput)
                .add(right.scale(strafeInput))
                .add(0.0D, verticalInput, 0.0D);
        Vec3 velocity = player.getDeltaMovement();
        Vec3 nextVelocity;
        if (direction.lengthSqr() < 1.0E-4D) {
            nextVelocity = velocity.scale(stage == 1 ? 0.76D : 0.985D);
        } else {
            Vec3 desired = direction.normalize().scale(FLIGHT_SPEEDS[stage]);
            double responsiveness = stage == 1 ? 0.24D : (stage == 2 ? 0.24D : 0.26D);
            if (velocity.lengthSqr() > 0.04D && velocity.dot(desired) < 0.0D) {
                // Stronger reverse thrust prevents long drifts when changing direction.
                responsiveness = Math.max(responsiveness, 0.42D);
            }
            nextVelocity = velocity.scale(1.0D - responsiveness).add(desired.scale(responsiveness));
        }

        // Use actual velocity with a small hysteresis band, so the pose follows
        // acceleration and remains off while hovering or stopped.
        double speed = nextVelocity.length();
        if (!player.isFallFlying() && speed >= 0.72D) {
            player.startFallFlying();
        } else if (player.isFallFlying() && speed <= 0.54D) {
            player.stopFallFlying();
        }

        player.setDeltaMovement(nextVelocity);
        player.yBodyRot = player.getYRot() + (float) strafeInput * (player.isFallFlying() ? 28.0F : 12.0F);
        player.hurtMarked = true;
    }
}
