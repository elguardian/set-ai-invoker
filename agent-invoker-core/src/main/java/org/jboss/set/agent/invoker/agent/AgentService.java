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

import org.jboss.set.agent.invoker.model.ApplicationEvent;

import java.util.function.Consumer;

/**
 * Unattended AI agent that analyses an application event and streams the response
 * line by line. Implementations delegate to an external CLI tool via ProcessBuilder.
 */
public interface AgentService {

    /** Unique name identifying this agent, e.g. {@code "claude"} or {@code "ibm-bob"}. */
    String getName();

    /**
     * Processes an event, streaming each output line to {@code outputLine} as it arrives.
     * Returns the full accumulated analysis once the agent process completes.
     *
     * @param verbose when true, passes verbose flags to the underlying agent CLI and allows
     *                the caller to display streamed output
     */
    AgentResponse process(ApplicationEvent event, String prompt, Consumer<String> outputLine, boolean verbose);
}
