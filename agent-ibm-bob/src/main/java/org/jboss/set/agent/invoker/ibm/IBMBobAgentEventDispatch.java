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

package org.jboss.set.agent.invoker.ibm;

import com.fasterxml.jackson.databind.JsonNode;
import org.jboss.set.agent.invoker.agent.AgentEventDispatch;
import org.jboss.set.agent.invoker.agent.DispatchResult;

/**
 * Handles IBM Bob CLI stream-json events.
 *
 * <p>Closes stdin when the {@code result} event arrives (end of a conversation turn),
 * which causes Bob to exit cleanly. Tool-use events are logged; actual tool
 * execution is handled internally by Bob CLI (built-ins) or via MCP servers.
 *
 * <p>To support multi-turn conversations, override the {@code result} branch
 * to return {@link DispatchResult#reply(String)} with the follow-up message
 * instead of {@link DispatchResult#done()}.
 */
public class IBMBobAgentEventDispatch implements AgentEventDispatch {

    @Override
    public DispatchResult dispatch(JsonNode event) {
        String type = event.path("type").asText();

        if ("result".equals(type)) {
            Log.LOG.turnComplete(event.path("subtype").asText());
            return DispatchResult.done();
        }

        if ("assistant".equals(type)) {
            JsonNode content = event.path("message").path("content");
            if (content.isArray()) {
                for (JsonNode block : content) {
                    if ("tool_use".equals(block.path("type").asText())) {
                        Log.LOG.toolUseEvent(block.path("name").asText());
                    }
                }
            }
        }

        return DispatchResult.skip();
    }
}
