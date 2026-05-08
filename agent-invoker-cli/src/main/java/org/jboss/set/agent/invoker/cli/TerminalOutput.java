package org.jboss.set.agent.invoker.cli;

import org.jboss.set.agent.invoker.agent.AgentResponse;
import org.jboss.set.agent.invoker.model.ApplicationEvent;
import org.jboss.set.agent.invoker.model.EventSourceConfig;
import org.jboss.set.agent.invoker.model.JiraIssueEvent;
import org.jboss.set.agent.invoker.model.PullRequestEvent;
import org.jboss.set.agent.invoker.model.StateEntry;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.io.PrintWriter;
import java.net.URI;
import java.util.List;

public class TerminalOutput {

    private final Terminal terminal;
    private final PrintWriter out;

    public TerminalOutput(Terminal terminal) {
        this.terminal = terminal;
        this.out = terminal.writer();
    }

    public void info(String msg) {
        println(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN), "✔ ", msg);
    }

    public void warn(String msg) {
        println(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW), "⚠ ", msg);
    }

    public void error(String msg) {
        println(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED), "✖ ", msg);
    }

    public void header(String msg) {
        out.println();
        out.println(new AttributedStringBuilder()
            .style(AttributedStyle.BOLD.foreground(AttributedStyle.CYAN))
            .append("── " + msg + " ")
            .toAnsi(terminal));
    }

    public void printEvent(int index, int total, ApplicationEvent event) {
        String label = switch (event) {
            case PullRequestEvent pr ->
                "PR #" + pr.prNumber() + " [" + pr.owner() + "/" + pr.repo() + "] " + pr.action();
            case JiraIssueEvent jira ->
                jira.issueKey() + " [" + jira.status() + "] " + jira.summary();
        };
        out.println(new AttributedStringBuilder()
            .style(AttributedStyle.BOLD)
            .append("[" + index + "/" + total + "] ")
            .style(AttributedStyle.DEFAULT)
            .append(label)
            .toAnsi(terminal));
    }

    /** Called for each streamed line of agent output. */
    public void agentLine(String agentName, String line) {
        out.println(new AttributedStringBuilder()
            .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.MAGENTA))
            .append("  [" + agentName + "] ")
            .style(AttributedStyle.DEFAULT)
            .append(line)
            .toAnsi(terminal));
        terminal.flush();
    }

    public void printResponse(AgentResponse response) {
        // Summary line after streaming is complete
        out.println();
    }

    public void printConfig(URI configUri, List<EventSourceConfig> entries) {
        header("Config: " + configUri);
        if (entries.isEmpty()) {
            out.println("  (empty)");
        } else {
            for (int i = 0; i < entries.size(); i++) {
                EventSourceConfig e = entries.get(i);
                out.println("  [" + i + "] type=" + e.eventType()
                    + "  url=" + e.eventUrl()
                    + (e.eventUser() != null ? "  user=" + e.eventUser() : "")
                    + (e.eventApiToken() != null ? "  api-token=" + e.eventApiToken() : "")
                    + (e.eventPassword() != null ? "  password=" + e.eventPassword() : "")
                    + (!e.eventFilter().isEmpty() ? "  filter=" + e.eventFilter() : ""));
            }
        }
        terminal.flush();
    }

    public void printState(URI stateUri, List<StateEntry> entries) {
        header("State: " + stateUri);
        if (entries.isEmpty()) {
            out.println("  (empty)");
        } else {
            for (StateEntry e : entries) {
                out.println("  type=" + e.eventType()
                    + "  url=" + e.eventUrl()
                    + "  checkpoint=" + e.eventCheckpoint());
            }
        }
        terminal.flush();
    }

    public PrintWriter writer() { return out; }

    public void flush() {
        terminal.flush();
    }

    private void println(AttributedStyle markStyle, String mark, String msg) {
        out.println(new AttributedStringBuilder()
            .style(markStyle)
            .append(mark)
            .style(AttributedStyle.DEFAULT)
            .append(msg)
            .toAnsi(terminal));
        terminal.flush();
    }
}
