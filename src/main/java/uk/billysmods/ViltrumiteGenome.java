package uk.billysmods;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class ViltrumiteGenome {
    private static final ResourceLocation ATTACK_DAMAGE_ID = id("genome_strength");
    private static final ResourceLocation ATTACK_KNOCKBACK_ID = id("genome_knockback");
    private static final ResourceLocation ARMOR_ID = id("genome_durability");
    private static final ResourceLocation TOUGHNESS_ID = id("genome_toughness");

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
        setModifier(player, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_ID, 19.0D, empowered);
        setModifier(player, Attributes.ATTACK_KNOCKBACK, ATTACK_KNOCKBACK_ID, 1.5D, empowered);
        setModifier(player, Attributes.ARMOR, ARMOR_ID, 20.0D, empowered);
        setModifier(player, Attributes.ARMOR_TOUGHNESS, TOUGHNESS_ID, 8.0D, empowered);
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

    private static void tickFlight(ServerPlayer player) {
        boolean empowered = hasGenome(player);
        Input input = player.getLastClientInput();

        if (!empowered) {
            player.setNoGravity(false);
            if (player.isFallFlying()) {
                player.stopFallFlying();
            }
            return;
        }

        // Press jump once airborne to transition into the weightless flight pose.
        if (input.jump() && !player.onGround() && !player.isFallFlying()) {
            player.startFallFlying();
        }
        if (!player.isFallFlying()) {
            player.setNoGravity(false);
            return;
        }

        player.setNoGravity(true);
        Vec3 look = player.getLookAngle();
        Vec3 right = new Vec3(look.z, 0.0D, -look.x);
        if (right.lengthSqr() > 1.0E-6D) {
            right = right.normalize();
        } else {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        }

        double forward = (input.forward() ? 1.0D : 0.0D) - (input.backward() ? 1.0D : 0.0D);
        double strafe = (input.right() ? 1.0D : 0.0D) - (input.left() ? 1.0D : 0.0D);
        Vec3 flightInput = look.scale(forward).add(right.scale(strafe));
        if (input.jump()) {
            flightInput = flightInput.add(0.0D, 0.55D, 0.0D);
        }
        if (input.shift()) {
            flightInput = flightInput.add(0.0D, -0.55D, 0.0D);
        }

        Vec3 velocity = player.getDeltaMovement();
        Vec3 nextVelocity;
        if (flightInput.lengthSqr() < 1.0E-4D) {
            nextVelocity = velocity.scale(0.78D);
        } else {
            Vec3 desiredVelocity = flightInput.normalize().scale(0.9D);
            nextVelocity = velocity.scale(0.82D).add(desiredVelocity.scale(0.18D));
        }
        player.setDeltaMovement(nextVelocity);
        player.yBodyRot = player.getYRot() + (float) strafe * 24.0F;
    }
}
