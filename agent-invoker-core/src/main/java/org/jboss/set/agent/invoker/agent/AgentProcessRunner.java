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

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Shared process execution helper for CLI-based agent implementations.
 *
 * <p>Starts the command from {@link AgentProcessRunnerParameters} and runs two concurrent
 * tasks: {@link StdinWriter} pipes the prompt (and any inline replies from
 * {@link AgentEventDispatch}) to stdin, while {@link StdoutReader} streams stdout lines
 * through the dispatch and to the console.
 */
public final class AgentProcessRunner {

    static final ObjectMapper PRETTY = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    /** Sentinel placed in the reply queue by {@link StdoutReader} to signal {@link StdinWriter} to close. */
    static final Optional<String> DONE = Optional.empty();

    private AgentProcessRunner() {}

    public static String run(AgentProcessRunnerParameters params) {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            ProcessBuilder pb = new ProcessBuilder(params.command());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BlockingQueue<Optional<String>> replies = new LinkedBlockingQueue<>();

            Future<Void>   stdinFuture  = executor.submit(new StdinWriter(process.getOutputStream(), params.prompt(), params.pipeStdin(), params.interactiveStdin(), replies));
            Future<String> stdoutFuture = executor.submit(new StdoutReader(process.getInputStream(), params.tag(), params.outputLine(), params.dispatch(), replies));
            executor.shutdown();

            if (!executor.awaitTermination(120, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                executor.shutdownNow();
                String msg = "[" + params.tag() + "] timed out after 120 s";
                params.outputLine().accept(msg);
                return msg;
            }

            String result = stdoutFuture.get();

            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                String msg = "[" + params.tag() + "] timed out after 120 s";
                params.outputLine().accept(msg);
                return msg;
            }
            if (process.exitValue() != 0) {
                Log.LOG.nonZeroExit(process.exitValue());
            }
            return result;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "[" + params.tag() + "] interrupted";
        } catch (Exception e) {
            Log.LOG.invocationError(e.getMessage());
            return "[" + params.tag() + "] error: " + e.getMessage();
        } finally {
            executor.shutdownNow();
        }
    }
}
