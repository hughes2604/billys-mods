package uk.billysmods;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class ViltrumiteGenome {
    private static final ResourceLocation ATTACK_DAMAGE_ID = id("genome_strength");
    private static final ResourceLocation ATTACK_KNOCKBACK_ID = id("genome_knockback");
    private static final ResourceLocation ARMOR_ID = id("genome_durability");
    private static final ResourceLocation TOUGHNESS_ID = id("genome_toughness");
    private static final ResourceLocation MAX_HEALTH_ID = id("genome_vitality");
    private static final Set<UUID> FLIGHT_ACTIVE = new HashSet<>();
    private static final Set<UUID> FLIGHT_BOOSTING = new HashSet<>();
    private static final Map<UUID, Boolean> SPRINT_BEFORE_FLIGHT_BOOST = new HashMap<>();
    private static final Map<UUID, Integer> PUNCH_FOV_TICKS = new HashMap<>();
    private static final double CRUISE_SPEED = 1.10D;
    private static final double BOOST_SPEED = 3.70D;
    private static final double REVERSE_SPEED = 0.90D;
    private static final double SUPERMAN_ENTRY_SPEED = 1.18D;
    private static final double SUPERMAN_EXIT_SPEED = 0.92D;

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
        setModifier(player, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_ID, 7.0D,
                empowered && isUnarmed(player));
        setModifier(player, Attributes.ATTACK_KNOCKBACK, ATTACK_KNOCKBACK_ID, 2.5D,
                empowered && isUnarmed(player));
        setModifier(player, Attributes.ARMOR, ARMOR_ID, 20.0D, empowered);
        setModifier(player, Attributes.ARMOR_TOUGHNESS, TOUGHNESS_ID, 12.0D, empowered);
        setModifier(player, Attributes.MAX_HEALTH, MAX_HEALTH_ID, 20.0D, empowered);
        if (empowered) {
            MobEffectInstance resistance = player.getEffect(MobEffects.RESISTANCE);
            if (resistance == null || resistance.getAmplifier() < 3 || resistance.getDuration() <= 20) {
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 40, 3, false, false, false));
            }
        }
        tickPunchFov(player);
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

    public static boolean isUnarmed(Player player) {
        return player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty();
    }

    public static void handleFlightControl(ServerPlayer player, int action) {
        if (!hasGenome(player)) return;
        UUID playerId = player.getUUID();

        if (action == 0) {
            if (FLIGHT_ACTIVE.contains(playerId)) {
                stopFlight(player, playerId);
                player.displayClientMessage(Component.literal("VILTRUMITE FLIGHT OFF").withStyle(ChatFormatting.GRAY), true);
            } else {
                FLIGHT_ACTIVE.add(playerId);
                player.setNoGravity(true);
                player.stopFallFlying();
                publishFlightState(player, true);
                player.displayClientMessage(Component.literal("VILTRUMITE FLIGHT ON").withStyle(ChatFormatting.AQUA), true);
            }
            return;
        }

        if (!FLIGHT_ACTIVE.contains(playerId)) return;
        if (action == 1 && FLIGHT_BOOSTING.add(playerId)) {
            SPRINT_BEFORE_FLIGHT_BOOST.put(playerId, player.isSprinting());
            player.setSprinting(true);
            publishFlightState(player, true);
        } else if (action == 2 && FLIGHT_BOOSTING.remove(playerId)) {
            restoreSprintAfterBoost(player, playerId);
            publishFlightState(player, true);
        }
    }

    private static void restoreSprintAfterBoost(ServerPlayer player, UUID playerId) {
        Boolean wasSprinting = SPRINT_BEFORE_FLIGHT_BOOST.remove(playerId);
        if (Boolean.FALSE.equals(wasSprinting) && !PUNCH_FOV_TICKS.containsKey(playerId)) {
            player.setSprinting(false);
        }
    }

    public static void pulsePunchFov(ServerPlayer player) {
        UUID playerId = player.getUUID();
        if (!player.isSprinting() && !FLIGHT_BOOSTING.contains(playerId)) {
            player.setSprinting(true);
            PUNCH_FOV_TICKS.put(playerId, 4);
        }
    }

    private static void tickPunchFov(ServerPlayer player) {
        UUID playerId = player.getUUID();
        Integer ticks = PUNCH_FOV_TICKS.get(playerId);
        if (ticks == null) return;
        if (ticks <= 1) {
            PUNCH_FOV_TICKS.remove(playerId);
            if (!FLIGHT_BOOSTING.contains(playerId)) player.setSprinting(false);
        } else {
            PUNCH_FOV_TICKS.put(playerId, ticks - 1);
        }
    }

    public static void stopFlightOnDisconnect(ServerPlayer player) {
        UUID playerId = player.getUUID();
        if (FLIGHT_ACTIVE.remove(playerId)) {
            FLIGHT_BOOSTING.remove(playerId);
            restoreSprintAfterBoost(player, playerId);
            PUNCH_FOV_TICKS.remove(playerId);
            player.setNoGravity(false);
            player.stopFallFlying();
        }
    }

    public static void syncFlightState(ServerPlayer viewer) {
        for (ServerPlayer subject : ((ServerLevel) viewer.level()).getServer().getPlayerList().getPlayers()) {
            if (FLIGHT_ACTIVE.contains(subject.getUUID()) && subject.level() == viewer.level()) {
                ServerPlayNetworking.send(viewer, new FlightStatePayload(subject.getId(), true,
                        FLIGHT_BOOSTING.contains(subject.getUUID())));
            }
        }
    }

    public static void syncTrackedPlayer(ServerPlayer subject, ServerPlayer viewer) {
        if (FLIGHT_ACTIVE.contains(subject.getUUID())) {
            ServerPlayNetworking.send(viewer, new FlightStatePayload(subject.getId(), true,
                    FLIGHT_BOOSTING.contains(subject.getUUID())));
        }
    }

    private static void publishFlightState(ServerPlayer subject, boolean active) {
        boolean boosting = active && FLIGHT_BOOSTING.contains(subject.getUUID());
        for (ServerPlayer viewer : ((ServerLevel) subject.level()).getServer().getPlayerList().getPlayers()) {
            if (viewer.level() == subject.level()) {
                ServerPlayNetworking.send(viewer, new FlightStatePayload(subject.getId(), active, boosting));
            }
        }
    }

    private static void stopFlight(ServerPlayer player, UUID playerId) {
        FLIGHT_ACTIVE.remove(playerId);
        FLIGHT_BOOSTING.remove(playerId);
        restoreSprintAfterBoost(player, playerId);
        PUNCH_FOV_TICKS.remove(playerId);
        publishFlightState(player, false);
        player.setNoGravity(false);
        player.stopFallFlying();
        player.setDeltaMovement(player.getDeltaMovement().scale(0.2D));
    }

    private static void tickFlight(ServerPlayer player) {
        UUID playerId = player.getUUID();
        if (!hasGenome(player)) {
            if (FLIGHT_ACTIVE.contains(playerId)) stopFlight(player, playerId);
            return;
        }
        if (!FLIGHT_ACTIVE.contains(playerId)) return;

        player.setNoGravity(true);
        Input input = player.getLastClientInput();
        Vec3 look = player.getLookAngle().normalize();
        float yawRadians = player.getYRot() * ((float) Math.PI / 180.0F);
        Vec3 right = new Vec3(-Math.cos(yawRadians), 0.0D, -Math.sin(yawRadians));
        double forwardInput = (input.forward() ? 1.0D : 0.0D) - (input.backward() ? 1.0D : 0.0D);
        double strafeInput = (input.right() ? 1.0D : 0.0D) - (input.left() ? 1.0D : 0.0D);
        double verticalInput = (input.jump() ? 1.0D : 0.0D) - (input.shift() ? 1.0D : 0.0D);
        Vec3 direction = look.scale(forwardInput).add(right.scale(strafeInput)).add(0.0D, verticalInput, 0.0D);

        Vec3 velocity = player.getDeltaMovement();
        Vec3 desired = Vec3.ZERO;
        boolean reversing = forwardInput < 0.0D;
        if (direction.lengthSqr() >= 1.0E-4D) {
            double topSpeed = reversing ? REVERSE_SPEED
                    : (FLIGHT_BOOSTING.contains(playerId) && forwardInput > 0.0D ? BOOST_SPEED : CRUISE_SPEED);
            desired = direction.normalize().scale(topSpeed);
        }
        boolean boostingForward = FLIGHT_BOOSTING.contains(playerId) && forwardInput > 0.0D;
        double acceleration = boostingForward ? 0.18D : 0.13D;
        if (velocity.lengthSqr() > 0.04D && velocity.dot(desired) < 0.0D) {
            acceleration = 0.30D;
        }
        Vec3 nextVelocity = velocity.scale(1.0D - acceleration).add(desired.scale(acceleration));
        double speed = nextVelocity.length();
        double forwardSpeed = nextVelocity.dot(look);
        if (!player.isFallFlying() && forwardSpeed >= SUPERMAN_ENTRY_SPEED) {
            player.startFallFlying();
        } else if (player.isFallFlying() && forwardSpeed <= SUPERMAN_EXIT_SPEED) {
            player.stopFallFlying();
        }

        player.setDeltaMovement(nextVelocity);
        player.yBodyRot = player.getYRot() + (float) strafeInput * (player.isFallFlying() ? 28.0F : 12.0F);
        player.hurtMarked = true;

        if (boostingForward && speed > 0.8D && player.tickCount % 3 == 0) {
            ServerLevel level = (ServerLevel) player.level();
            level.sendParticles(ParticleTypes.CLOUD,
                    player.getX() - nextVelocity.x * 0.28D,
                    player.getY() + player.getBbHeight() * 0.45D - nextVelocity.y * 0.12D,
                    player.getZ() - nextVelocity.z * 0.28D,
                    1, 0.04D, 0.04D, 0.04D, 0.01D);
        }
    }
}
