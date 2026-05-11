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

import java.util.List;
import java.util.function.Consumer;

/**
 * Immutable parameters for {@link AgentProcessRunner#run(AgentProcessRunnerParameters)}.
 * Construct via {@link #builder()}.
 */
public final class AgentProcessRunnerParameters {

    private final List<String>       command;
    private final String             prompt;
    private final boolean            pipeStdin;
    private final boolean            interactiveStdin;
    private final String             tag;
    private final Consumer<String>   outputLine;
    private final AgentEventDispatch dispatch;

    private AgentProcessRunnerParameters(Builder b) {
        this.command          = List.copyOf(b.command);
        this.prompt           = b.prompt != null ? b.prompt : "";
        this.pipeStdin        = b.pipeStdin;
        this.interactiveStdin = b.interactiveStdin;
        this.tag              = b.tag;
        this.outputLine       = b.outputLine;
        this.dispatch         = b.dispatch != null ? b.dispatch : AgentEventDispatch.none();
    }

    public List<String>       command()          { return command; }
    public String             prompt()           { return prompt; }
    public boolean            pipeStdin()        { return pipeStdin; }
    /** When true, stdin stays open after the initial prompt to allow inline replies. */
    public boolean            interactiveStdin() { return interactiveStdin; }
    public String             tag()              { return tag; }
    public Consumer<String>   outputLine()       { return outputLine; }
    public AgentEventDispatch dispatch()         { return dispatch; }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private List<String>       command;
        private String             prompt;
        private boolean            pipeStdin;
        private boolean            interactiveStdin;
        private String             tag;
        private Consumer<String>   outputLine;
        private AgentEventDispatch dispatch;

        private Builder() {}

        public Builder command(List<String> command)                   { this.command          = command;          return this; }
        public Builder prompt(String prompt)                           { this.prompt           = prompt;           return this; }
        public Builder pipeStdin(boolean pipeStdin)                    { this.pipeStdin        = pipeStdin;        return this; }
        public Builder interactiveStdin(boolean interactiveStdin)      { this.interactiveStdin = interactiveStdin; return this; }
        public Builder tag(String tag)                                 { this.tag              = tag;              return this; }
        public Builder outputLine(Consumer<String> outputLine)         { this.outputLine       = outputLine;       return this; }
        public Builder dispatch(AgentEventDispatch dispatch)           { this.dispatch         = dispatch;         return this; }

        public AgentProcessRunnerParameters build() {
            if (command == null || command.isEmpty()) throw new IllegalStateException("command is required");
            if (tag == null || tag.isBlank())         throw new IllegalStateException("tag is required");
            if (outputLine == null)                   throw new IllegalStateException("outputLine is required");
            if (dispatch == null) dispatch = AgentEventDispatch.none();
            return new AgentProcessRunnerParameters(this);
        }
    }
}
