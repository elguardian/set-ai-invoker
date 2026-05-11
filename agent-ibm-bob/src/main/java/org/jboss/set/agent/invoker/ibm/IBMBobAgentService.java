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

import org.jboss.set.agent.invoker.agent.AgentProcessRunner;
import org.jboss.set.agent.invoker.agent.AgentProcessRunnerParameters;
import org.jboss.set.agent.invoker.agent.AgentResponse;
import org.jboss.set.agent.invoker.agent.AgentService;
import org.jboss.set.agent.invoker.model.ApplicationEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class IBMBobAgentService implements AgentService {

    @Override
    public String getName() {
        return "ibm-bob";
    }

    @Override
    public AgentResponse process(ApplicationEvent event, String prompt, Consumer<String> outputLine) {
        String command = System.getenv().getOrDefault("IBM_BOB_COMMAND", "ibmcloud");
        String argsEnv = System.getenv().getOrDefault("IBM_BOB_ARGS", "ml,text-generation,--input");
        boolean stdin  = "true".equalsIgnoreCase(System.getenv("IBM_BOB_STDIN"));

        List<String> cmd = new ArrayList<>();
        cmd.add(command);
        Arrays.stream(argsEnv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).forEach(cmd::add);
        if (!stdin) cmd.add(prompt != null ? prompt : "");

        String analysis = AgentProcessRunner.run(
                AgentProcessRunnerParameters.builder()
                        .command(cmd)
                        .prompt(prompt != null ? prompt : "")
                        .pipeStdin(stdin)
                        .tag(getName())
                        .outputLine(outputLine)
                        .build());
        return new AgentResponse(getName(), event.eventId(), analysis, Instant.now());
    }
}
