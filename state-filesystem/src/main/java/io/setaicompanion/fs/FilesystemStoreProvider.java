package io.setaicompanion.fs;

import io.setaicompanion.store.ConfigStore;
import io.setaicompanion.store.StateStore;
import io.setaicompanion.store.StoreProvider;

public class FilesystemStoreProvider implements StoreProvider {

    private final JsonStateStore  stateStore  = new JsonStateStore();
    private final JsonConfigStore configStore = new JsonConfigStore();

    @Override
    public String name() {
        return "filesystem";
    }

    @Override
    public StateStore getStateStore() {
        return stateStore;
    }

    @Override
    public ConfigStore getConfigStore() {
        return configStore;
    }
}
