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

package org.jboss.set.agent.invoker.store;

import org.jboss.set.agent.invoker.store.ConfigStore;
import org.jboss.set.agent.invoker.store.StateStore;

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
