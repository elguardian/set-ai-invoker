package org.jboss.set.agent.invoker.ibm;

import org.jboss.set.agent.invoker.agent.AgentResponse;
import org.jboss.set.agent.invoker.agent.AgentService;
import org.jboss.set.agent.invoker.model.ApplicationEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class IBMBobAgentService implements AgentService {

    @Override
    public String getName() {
        return "ibm-bob";
    }

    @Override
    public AgentResponse process(ApplicationEvent event, String prompt, Consumer<String> outputLine) {
        String analysis = invokeIBMCLI(prompt != null ? prompt : "", outputLine);
        return new AgentResponse(getName(), event.eventId(), analysis, Instant.now());
    }

    private String invokeIBMCLI(String prompt, Consumer<String> outputLine) {
        String command = System.getenv().getOrDefault("IBM_BOB_COMMAND", "ibmcloud");
        String argsEnv = System.getenv().getOrDefault("IBM_BOB_ARGS", "ml,text-generation,--input");
        boolean stdin  = "true".equalsIgnoreCase(System.getenv("IBM_BOB_STDIN"));

        List<String> cmd = new ArrayList<>();
        cmd.add(command);
        Arrays.stream(argsEnv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).forEach(cmd::add);
        if (!stdin) cmd.add(prompt);

        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            CompletableFuture<Void> stdinWriter = null;
            if (stdin) {
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
                    outputLine.accept(line);
                    if (!sb.isEmpty()) sb.append('\n');
                    sb.append(line);
                }
            }

            if (stdinWriter != null) stdinWriter.join();

            boolean finished = process.waitFor(120, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                String msg = "[ibm-bob] timed out after 120 s";
                outputLine.accept(msg);
                return msg;
            }
            if (process.exitValue() != 0) {
                Log.LOG.nonZeroExit(process.exitValue());
            }

            String result = sb.toString().trim();
            return result.isBlank() ? "[ibm-bob] no output" : result;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "[ibm-bob] interrupted";
        } catch (Exception e) {
            Log.LOG.invocationError(e.getMessage());
            return "[ibm-bob] error: " + e.getMessage();
        }
    }
}
