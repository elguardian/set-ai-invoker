package io.setaicompanion.claude;

import io.setaicompanion.agent.AgentResponse;
import io.setaicompanion.agent.AgentService;
import io.setaicompanion.model.ApplicationEvent;
import io.setaicompanion.model.JiraFieldChangeEvent;
import io.setaicompanion.model.PullRequestEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ClaudeAgentService implements AgentService {

    @Override
    public String getName() {
        return "claude";
    }

    @Override
    public AgentResponse process(ApplicationEvent event, Consumer<String> outputLine) {
        String prompt = buildPrompt(event);
        String analysis = invokeClaudeCLI(prompt, outputLine);
        return new AgentResponse(getName(), event.eventId(), analysis, Instant.now());
    }

    private String invokeClaudeCLI(String prompt, Consumer<String> outputLine) {
        String command = System.getenv().getOrDefault("CLAUDE_COMMAND", "claude");
        String model   = System.getenv("CLAUDE_MODEL");

        try {
            ProcessBuilder pb = model != null
                ? new ProcessBuilder(command, "--print", "--no-color", "-m", model)
                : new ProcessBuilder(command, "--print", "--no-color");

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

    private String buildPrompt(ApplicationEvent event) {
        return switch (event) {
            case PullRequestEvent pr -> """
                You are an AI assistant monitoring GitHub pull requests. \
                Analyse the following event and provide a concise summary with any recommended actions.

                Repository : %s/%s
                PR #%d     : %s
                Author     : %s
                URL        : %s
                Description:
                %s
                """.formatted(pr.owner(), pr.repo(), pr.prNumber(), pr.title(),
                    pr.author(), pr.url(), pr.description());

            case JiraFieldChangeEvent jira -> """
                You are an AI assistant monitoring Jira issue changes. \
                The fields pm_ack, dev_ack, and qe_ack track acknowledgments \
                from Product Management, Development, and QE teams respectively. \
                Analyse the following change and suggest follow-up actions.

                Issue   : %s
                Field   : %s
                From    : %s
                To      : %s
                By      : %s
                When    : %s
                """.formatted(jira.issueKey(), jira.fieldName(),
                    jira.oldValue(), jira.newValue(), jira.changedBy(), jira.timestamp());
        };
    }
}
