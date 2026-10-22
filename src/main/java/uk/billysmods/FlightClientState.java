package uk.billysmods;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.multiplayer.ClientLevel;

public final class FlightClientState {
    private static final Set<Integer> FLYING_ENTITY_IDS = ConcurrentHashMap.newKeySet();

    private FlightClientState() { }

    public static void setFlying(int entityId, boolean active) {
        if (active) {
            FLYING_ENTITY_IDS.add(entityId);
        } else {
            FLYING_ENTITY_IDS.remove(entityId);
        }
    }

    public static boolean isFlying(int entityId) {
        return FLYING_ENTITY_IDS.contains(entityId);
    }

    public static void removeMissing(ClientLevel level) {
        if (level == null) {
            FLYING_ENTITY_IDS.clear();
            return;
        }
        FLYING_ENTITY_IDS.removeIf(entityId -> level.getEntity(entityId) == null);
    }

    public static void clear() {
        FLYING_ENTITY_IDS.clear();
    }
}
