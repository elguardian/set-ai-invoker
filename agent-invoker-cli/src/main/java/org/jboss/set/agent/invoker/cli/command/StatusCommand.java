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
import picocli.CommandLine.ParentCommand;

@Command(name = "status", description = "Show current session configuration")
public class StatusCommand implements Runnable {

    @ParentCommand CompanionCLI root;

    @Override
    public void run() {
        root.out.header("Current session");
        root.terminal.writer().println("  Config URI    : " + root.configUri);
        root.terminal.writer().println("  State URI     : " + root.stateUri);
        root.terminal.writer().println("  Store         : " + (root.storeImpl      != null ? root.storeImpl      : "(auto)"));
        root.terminal.writer().println("  Marshaller    : " + (root.marshallerImpl != null ? root.marshallerImpl : "(auto)"));
        root.terminal.writer().println("  Agent         : " + root.agentName);
        root.terminal.flush();
    }
}
