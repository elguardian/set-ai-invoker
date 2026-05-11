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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * Shared process execution helper for CLI-based agent implementations.
 *
 * <p>Starts {@code command} and runs two concurrent tasks: {@link StdinWriter} pipes
 * the prompt (and any inline replies from {@link AgentEventDispatch}) to stdin, while
 * {@link StdoutReader} streams stdout lines through the dispatch and to the console.
 */
public final class ProcessRunner {

    static final ObjectMapper PRETTY = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    /** Sentinel placed in the reply queue by {@link StdoutReader} to signal {@link StdinWriter} to close. */
    static final Optional<String> DONE = Optional.empty();

    private ProcessRunner() {}

    public static String run(List<String> command, String prompt, boolean pipeStdin,
                             String tag, Consumer<String> outputLine, AgentEventDispatch dispatch,
                             IntConsumer onNonZeroExit, Consumer<String> onError) {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BlockingQueue<Optional<String>> replies = new LinkedBlockingQueue<>();

            Future<Void>   stdinFuture  = executor.submit(new StdinWriter(process.getOutputStream(), prompt, pipeStdin, replies));
            Future<String> stdoutFuture = executor.submit(new StdoutReader(process.getInputStream(), tag, outputLine, dispatch, replies));

            try {
                executor.awaitTermination(120, TimeUnit.SECONDS);
                String result = stdoutFuture.get();

                boolean finished = process.waitFor(5, TimeUnit.SECONDS);
                if (!finished) {
                    process.destroyForcibly();
                    String msg = "[" + tag + "] timed out after 120 s";
                    outputLine.accept(msg);
                    return msg;
                }
                if (process.exitValue() != 0) {
                    onNonZeroExit.accept(process.exitValue());
                }
                return result;

            } catch (TimeoutException e) {
                process.destroyForcibly();
                executor.shutdownNow();
                String msg = "[" + tag + "] timed out after 120 s";
                outputLine.accept(msg);
                return msg;
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "[" + tag + "] interrupted";
        } catch (Exception e) {
            onError.accept(e.getMessage());
            return "[" + tag + "] error: " + e.getMessage();
        } finally {
            executor.shutdownNow();
        }
    }
}
