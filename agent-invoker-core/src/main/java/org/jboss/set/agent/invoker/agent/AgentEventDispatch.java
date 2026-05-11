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

package org.jboss.set.agent.invoker.agent;

import java.util.Optional;

/**
 * Handles individual lines streamed from an agent process stdout.
 *
 * <p>Implementations inspect each raw line and optionally return a reply
 * to be written back to the process stdin, enabling inline command responses.
 */
public interface AgentEventDispatch {

    /**
     * Called for each line read from the process stdout.
     *
     * @param rawLine the raw line as received from the process
     * @return a non-empty Optional if a reply should be written to stdin
     */
    Optional<String> dispatch(String rawLine);

    /** No-op dispatch that never replies to stdin. */
    static AgentEventDispatch none() {
        return rawLine -> Optional.empty();
    }
}
