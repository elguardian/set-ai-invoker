package io.setaicompanion.store;

import io.setaicompanion.store.ConfigStore;
import io.setaicompanion.store.StateStore;

/**
 * Pluggable store module discovered by {@link java.util.ServiceLoader}.
 *
 * <p>A single provider supplies both a {@link StateStore} and a {@link ConfigStore},
 * keeping state and config storage in the same backend within the same run.
 */
public interface StoreProvider {

    /** Short identifier, e.g. {@code "filesystem"} or {@code "github"}. */
    String name();

    StateStore getStateStore();

    ConfigStore getConfigStore();
}
