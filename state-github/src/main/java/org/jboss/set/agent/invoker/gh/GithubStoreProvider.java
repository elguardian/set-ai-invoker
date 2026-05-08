package org.jboss.set.agent.invoker.gh;

import org.jboss.set.agent.invoker.store.ConfigStore;
import org.jboss.set.agent.invoker.store.StateStore;
import org.jboss.set.agent.invoker.store.StoreProvider;

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
