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

/**
 * Result returned by {@link AgentEventDispatch#dispatch(com.fasterxml.jackson.databind.JsonNode)}.
 *
 * <ul>
 *   <li>{@link #skip()} — no action, continue reading</li>
 *   <li>{@link #reply(String)} — write text to stdin and continue</li>
 *   <li>{@link #done()} — signal {@link StdinWriter} to close stdin and terminate the session</li>
 * </ul>
 */
public final class DispatchResult {

    private enum Kind { SKIP, REPLY, DONE }

    private static final DispatchResult SKIP_INSTANCE = new DispatchResult(Kind.SKIP, null);
    private static final DispatchResult DONE_INSTANCE = new DispatchResult(Kind.DONE, null);

    private final Kind   kind;
    private final String text;

    private DispatchResult(Kind kind, String text) {
        this.kind = kind;
        this.text = text;
    }

    public static DispatchResult skip()             { return SKIP_INSTANCE; }
    public static DispatchResult done()             { return DONE_INSTANCE; }
    public static DispatchResult reply(String text) { return new DispatchResult(Kind.REPLY, text); }

    public boolean isSkip()  { return kind == Kind.SKIP; }
    public boolean isDone()  { return kind == Kind.DONE; }
    public boolean isReply() { return kind == Kind.REPLY; }
    public String  text()    { return text; }
}
