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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * Shared process execution helper for CLI-based agent implementations.
 *
 * <p>Starts {@code command}, optionally pipes {@code prompt} to stdin, streams every
 * stdout line to {@code outputLine}, waits up to 120 s, and returns the full output.
 *
 * @param onNonZeroExit called with the exit code when the process exits non-zero
 * @param onError       called with the exception message on unexpected failure
 */
public final class ProcessRunner {

    private static final ObjectMapper PRETTY = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private ProcessRunner() {}

    private static void emit(String line, String tag, Consumer<String> outputLine) {
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
            JsonNode node = PRETTY.readTree(line);
            outputLine.accept("[" + tag + "]");
            for (String prettyLine : PRETTY.writeValueAsString(node).split("\n")) {
                outputLine.accept(prettyLine);
            }
        } catch (Exception e) {
            outputLine.accept(line);
        }
    }

    public static String run(List<String> command, String prompt, boolean pipeStdin,
                             String tag, Consumer<String> outputLine,
                             IntConsumer onNonZeroExit, Consumer<String> onError) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            CompletableFuture<Void> stdinWriter = null;
            if (pipeStdin) {
                stdinWriter = CompletableFuture.runAsync(() -> {
                    try (OutputStream os = process.getOutputStream()) {
                        os.write(prompt.getBytes(StandardCharsets.UTF_8));
                    } catch (IOException ignored) {}
                });
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    emit(line, tag, outputLine);
                    if (!sb.isEmpty()) sb.append('\n');
                    sb.append(line);
                }
            }

            if (stdinWriter != null) stdinWriter.join();

            boolean finished = process.waitFor(120, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                String msg = "[" + tag + "] timed out after 120 s";
                outputLine.accept(msg);
                return msg;
            }
            if (process.exitValue() != 0) {
                onNonZeroExit.accept(process.exitValue());
            }

            String result = sb.toString().trim();
            return result.isBlank() ? "[" + tag + "] no output" : result;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "[" + tag + "] interrupted";
        } catch (Exception e) {
            onError.accept(e.getMessage());
            return "[" + tag + "] error: " + e.getMessage();
        }
    }
}
