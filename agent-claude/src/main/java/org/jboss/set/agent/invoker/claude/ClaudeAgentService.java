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

import org.jboss.set.agent.invoker.agent.AgentEventDispatch;
import org.jboss.set.agent.invoker.agent.AgentResponse;
import org.jboss.set.agent.invoker.agent.AgentService;
import org.jboss.set.agent.invoker.agent.ProcessRunner;
import org.jboss.set.agent.invoker.model.ApplicationEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

public class ClaudeAgentService implements AgentService {

    @Override
    public String getName() {
        return "claude";
    }

    @Override
    public AgentResponse process(ApplicationEvent event, String prompt, Consumer<String> outputLine) {
        String command = System.getenv().getOrDefault("CLAUDE_COMMAND", "claude");
        String model   = System.getenv("CLAUDE_MODEL");

        List<String> cmd = Stream.of(command, "--verbose", "--print", "--output-format", "stream-json")
                .collect(toCollection(ArrayList::new));
        if (model != null) {
            cmd.add("-m");
            cmd.add(model);
        }

        String analysis = ProcessRunner.run(cmd, prompt != null ? prompt : "", true,
                getName(), outputLine, AgentEventDispatch.none(), Log.LOG::nonZeroExit, Log.LOG::invocationError);
        return new AgentResponse(getName(), event.eventId(), analysis, Instant.now());
    }
}
