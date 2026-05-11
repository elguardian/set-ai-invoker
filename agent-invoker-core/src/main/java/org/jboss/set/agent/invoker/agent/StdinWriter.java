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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

/**
 * Writes the initial prompt to stdin, then relays any inline replies
 * queued by {@link StdoutReader} until the {@link ProcessRunner#DONE} sentinel arrives.
 */
public final class StdinWriter implements Callable<Void> {

    private final OutputStream stdin;
    private final String prompt;
    private final boolean pipeStdin;
    private final BlockingQueue<Optional<String>> replies;

    public StdinWriter(OutputStream stdin, String prompt, boolean pipeStdin,
                       BlockingQueue<Optional<String>> replies) {
        this.stdin     = stdin;
        this.prompt    = prompt;
        this.pipeStdin = pipeStdin;
        this.replies   = replies;
    }

    @Override
    public Void call() {
        try (OutputStream os = stdin) {
            if (pipeStdin) {
                os.write(prompt.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
            // stdin closed here via try-with-resources, sending EOF to the process
        } catch (IOException e) {
            Thread.currentThread().interrupt();
        }
        // Drain replies queue until StdoutReader signals DONE
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Optional<String> reply = replies.take();
                if (reply.isEmpty()) break;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }
}
