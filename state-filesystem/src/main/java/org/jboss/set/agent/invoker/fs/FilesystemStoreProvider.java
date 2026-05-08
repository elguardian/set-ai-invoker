package org.jboss.set.agent.invoker.fs;

import org.jboss.set.agent.invoker.store.ConfigStore;
import org.jboss.set.agent.invoker.store.StateStore;
import org.jboss.set.agent.invoker.store.StoreProvider;

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
