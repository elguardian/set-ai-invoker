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

package org.jboss.set.agent.invoker.claude;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.set.agent.invoker.agent.AgentProcessRunner;
import org.jboss.set.agent.invoker.agent.AgentProcessRunnerParameters;
import org.jboss.set.agent.invoker.agent.AgentResponse;
import org.jboss.set.agent.invoker.agent.AgentService;
import org.jboss.set.agent.invoker.model.ApplicationEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ClaudeAgentService implements AgentService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String getName() {
        return "claude";
    }

    @Override
    public AgentResponse process(ApplicationEvent event, String prompt, Consumer<String> outputLine, boolean verbose) {
        String command      = System.getenv().getOrDefault("CLAUDE_COMMAND", "claude");
        String model        = System.getenv().get("CLAUDE_MODEL");
        String allowedTools = System.getenv().getOrDefault("CLAUDE_ALLOWED_TOOLS", "Bash,WebSearch,Read");

        List<String> cmd = new ArrayList<>();
        cmd.add(command);
        cmd.add("-p");
        cmd.add(prompt != null ? prompt : "");
        if (verbose) {
            cmd.add("--verbose");
            cmd.add("--output-format");
            cmd.add("stream-json");
        }
        cmd.add("--allowedTools");
        cmd.add(allowedTools);
        if (model != null && !model.isBlank()) {
            cmd.add("--model");
            cmd.add(model);
        }

        String analysis = AgentProcessRunner.run(
                AgentProcessRunnerParameters.builder()
                        .command(cmd)
                        .pipeStdin(false)
                        .tag(getName())
                        .outputLine(outputLine)
                        .dispatch(new ClaudeAgentEventDispatch())
                        .build());

        String text = verbose ? extractResultText(analysis) : analysis;
        return new AgentResponse(getName(), event.eventId(),
                text.isBlank() ? "[claude] no output" : text, Instant.now());
    }

    private static String extractResultText(String streamJson) {
        for (String line : streamJson.split("\n")) {
            try {
                JsonNode node = MAPPER.readTree(line);
                if ("result".equals(node.path("type").asText())) {
                    String result = node.path("result").asText();
                    if (!result.isBlank()) return result;
                }
            } catch (Exception ignored) {}
        }
        return streamJson;
    }
}
