package io.setaicompanion.gh;

import io.setaicompanion.store.ConfigStore;
import io.setaicompanion.store.StateStore;
import io.setaicompanion.store.StoreProvider;

public class GithubStoreProvider implements StoreProvider {

    private final GithubStateStore  stateStore  = new GithubStateStore();
    private final GithubConfigStore configStore = new GithubConfigStore();

    @Override
    public String name() {
        return "github";
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
