/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
