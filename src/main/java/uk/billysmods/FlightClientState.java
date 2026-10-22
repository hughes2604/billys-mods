package uk.billysmods;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.multiplayer.ClientLevel;

public final class FlightClientState {
    private static final Set<Integer> FLYING_ENTITY_IDS = ConcurrentHashMap.newKeySet();
    private static final Set<Integer> BOOSTING_ENTITY_IDS = ConcurrentHashMap.newKeySet();

    private FlightClientState() { }

    public static void setFlight(int entityId, boolean active, boolean boosting) {
        if (active) FLYING_ENTITY_IDS.add(entityId);
        else FLYING_ENTITY_IDS.remove(entityId);
        if (active && boosting) BOOSTING_ENTITY_IDS.add(entityId);
        else BOOSTING_ENTITY_IDS.remove(entityId);
    }

    public static boolean isFlying(int entityId) {
        return FLYING_ENTITY_IDS.contains(entityId);
    }

    public static boolean isBoosting(int entityId) {
        return BOOSTING_ENTITY_IDS.contains(entityId);
    }

    public static void removeMissing(ClientLevel level) {
        if (level == null) {
            FLYING_ENTITY_IDS.clear();
        BOOSTING_ENTITY_IDS.clear();
            return;
        }
        FLYING_ENTITY_IDS.removeIf(entityId -> level.getEntity(entityId) == null);
        BOOSTING_ENTITY_IDS.removeIf(entityId -> level.getEntity(entityId) == null);
    }

    public static void clear() {
        FLYING_ENTITY_IDS.clear();
    }
}
