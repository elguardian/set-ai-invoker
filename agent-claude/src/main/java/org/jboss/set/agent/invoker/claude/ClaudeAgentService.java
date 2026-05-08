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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.jboss.set.agent.invoker.agent.AgentResponse;
import org.jboss.set.agent.invoker.agent.AgentService;
import org.jboss.set.agent.invoker.model.ApplicationEvent;

import static java.util.stream.Collectors.toCollection;

public class ClaudeAgentService implements AgentService {

    @Override
    public String getName() {
        return "claude";
    }

    @Override
    public AgentResponse process(ApplicationEvent event, String prompt, Consumer<String> outputLine) {
        String analysis = invokeClaudeCLI(prompt != null ? prompt : "", outputLine);
        return new AgentResponse(getName(), event.eventId(), analysis, Instant.now());
    }

    private String invokeClaudeCLI(String prompt, Consumer<String> outputLine) {
        String command = System.getenv().getOrDefault("CLAUDE_COMMAND", "claude");
        String model   = System.getenv("CLAUDE_MODEL");
        try {
            List<String> args = Stream.of(command, "--verbose", "--print", "--output-format", "stream-json").collect(toCollection(ArrayList::new));
            if(model != null) {
                args.add("-m");
                args.add(model);
            }
            ProcessBuilder pb = new ProcessBuilder(args);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            CompletableFuture<Void> stdinWriter = CompletableFuture.runAsync(() -> {
                try (OutputStream os = process.getOutputStream()) {
                    os.write(prompt.getBytes(StandardCharsets.UTF_8));
                } catch (IOException ignored) {}
            });

            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputLine.accept(line);
                    if (!sb.isEmpty()) sb.append('\n');
                    sb.append(line);
                }
            }
            stdinWriter.join();

            boolean finished = process.waitFor(120, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                String msg = "[claude] timed out after 120 s";
                outputLine.accept(msg);
                return msg;
            }
            if (process.exitValue() != 0) {
                Log.LOG.nonZeroExit(process.exitValue());
            }

            String result = sb.toString().trim();
            return result.isBlank() ? "[claude] no output" : result;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "[claude] interrupted";
        } catch (Exception e) {
            Log.LOG.invocationError(e.getMessage());
            return "[claude] error: " + e.getMessage();
        }
    }

}
