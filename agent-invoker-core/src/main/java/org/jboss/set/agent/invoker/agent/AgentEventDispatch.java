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

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

/**
 * Handles individual events streamed from an agent process stdout.
 *
 * <p>Implementations inspect each parsed JSON event and optionally return a reply
 * to be written back to the process stdin, enabling inline command responses.
 * Lines that are not valid JSON are not dispatched.
 */
public interface AgentEventDispatch {

    /**
     * Called for each JSON event parsed from the process stdout.
     *
     * @param event the parsed JSON event
     * @return a non-empty Optional if a reply should be written to stdin
     */
    Optional<String> dispatch(JsonNode event);

    /** Logs each event at DEBUG level and never replies to stdin. */
    static AgentEventDispatch none() {
        return event -> {
            Log.LOG.noHandlerForEvent(event.toString());
            return Optional.of("continue");
        };
    }
}
