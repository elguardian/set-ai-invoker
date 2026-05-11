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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Reads stdout line by line, emits each line to the console (pretty-printing JSON),
 * delegates to {@link AgentEventDispatch} for optional stdin replies,
 * and signals {@link StdinWriter} to close when stdout reaches EOF.
 */
public final class StdoutReader implements Callable<String> {

    private final InputStream stdout;
    private final String tag;
    private final Consumer<String> outputLine;
    private final AgentEventDispatch dispatch;
    private final BlockingQueue<Optional<String>> replies;

    public StdoutReader(InputStream stdout, String tag, Consumer<String> outputLine,
                        AgentEventDispatch dispatch, BlockingQueue<Optional<String>> replies) {
        this.stdout     = stdout;
        this.tag        = tag;
        this.outputLine = outputLine;
        this.dispatch   = dispatch;
        this.replies    = replies;
    }

    @Override
    public String call() throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stdout, StandardCharsets.UTF_8))) {
            String line;
            while (!Thread.currentThread().isInterrupted() && (line = reader.readLine()) != null) {
                emit(line);
                dispatch.dispatch(line).ifPresent(reply -> replies.offer(Optional.of(reply)));
                if (!sb.isEmpty()) sb.append('\n');
                sb.append(line);
            }
        } finally {
            replies.offer(ProcessRunner.DONE);
        }
        String result = sb.toString().trim();
        return result.isBlank() ? "[" + tag + "] no output" : result;
    }

    private void emit(String line) {
        if (line == null || line.isBlank()) {
            outputLine.accept(line);
            return;
        }
        char first = line.stripLeading().charAt(0);
        if (first != '{' && first != '[') {
            outputLine.accept(line);
            return;
        }
        try {
            JsonNode node = ProcessRunner.PRETTY.readTree(line);
            outputLine.accept("[" + tag + "]");
            for (String prettyLine : ProcessRunner.PRETTY.writeValueAsString(node).split("\n")) {
                outputLine.accept(prettyLine);
            }
        } catch (Exception e) {
            outputLine.accept(line);
        }
    }
}
