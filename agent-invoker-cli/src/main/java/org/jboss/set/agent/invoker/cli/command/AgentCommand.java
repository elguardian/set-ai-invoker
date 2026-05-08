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

package org.jboss.set.agent.invoker.cli.command;

import org.jboss.set.agent.invoker.cli.CompanionCLI;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "agent", description = "Set the agent to use")
public class AgentCommand implements Runnable {

    @ParentCommand CompanionCLI root;
    @Parameters(index = "0", description = "Agent name (e.g. claude, ibm-bob)") String name;

    @Override
    public void run() {
        root.agentName = name;
        root.out.info("Agent set to: " + name);
    }
}
