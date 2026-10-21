package uk.billysmods;

import net.fabricmc.api.ClientModInitializer;

/** Client rendering is driven by the server-synchronised fall-flying pose. */
public final class ViltrumiteClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Flight controls and movement are authoritative on the server for multiplayer parity.
    }
}
