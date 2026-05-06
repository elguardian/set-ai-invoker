package io.setaicompanion.cli;

import io.setaicompanion.agent.AgentResponse;
import io.setaicompanion.model.ApplicationEvent;
import io.setaicompanion.model.EventSourceConfig;
import io.setaicompanion.model.JiraFieldChangeEvent;
import io.setaicompanion.model.PullRequestEvent;
import io.setaicompanion.model.StateEntry;
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
                "PR #" + pr.prNumber() + " [" + pr.owner() + "/" + pr.repo() + "] " + pr.title();
            case JiraFieldChangeEvent jira ->
                jira.issueKey() + " — " + jira.fieldName() + ": " + jira.oldValue() + " → " + jira.newValue();
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

    public void printHelp() {
        header("Set AI Companion — usage");
        out.println("  java -jar cli-runner.jar [OPTIONS]");
        out.println();
        out.println("General options:");
        out.println("  --config-uri URI            Config URI (env: CONFIG_URI, default: ./companion-config.json)");
        out.println("  --state-uri  URI            State URI  (env: STATE_URI,  default: ./companion-state.json)");
        out.println("  --config-impl NAME          Force config store implementation (e.g. filesystem, github)");
        out.println("  --state-impl  NAME          Force state store implementation  (e.g. filesystem, github)");
        out.println("  --agent NAME                Agent to use: claude | ibm-bob  (env: AGENT, default: claude)");
        out.println("  --help / -h                 Show this help");
        out.println();
        out.println("Config management:");
        out.println("  --config-show               Print all configured sources and exit");
        out.println("  --config-add type=<t> url=<u> [user=<u>] [api-token=<t>] [password=<p>]");
        out.println("                              Add a source to the config file");
        out.println("  --config-remove <type> <url>");
        out.println("                              Remove a source from the config file");
        out.println("  --config-filter <type> <url> [filter-tokens...]");
        out.println("                              Set collector-specific filter for a source");
        out.println("                              (no tokens = show filter help for that collector)");
        out.println();
        out.println("Collection:");
        out.println("  (no extra flags)            Collect from all sources in the config file");
        out.println("  --collect <type> <url>      Collect from one specific source");
        out.println("  --override-checkpoint VAL   Use this checkpoint instead of the stored one for this run");
        out.println();
        out.println("  If called with no arguments, an interactive REPL is started.");
        out.println();
        out.println("Interactive REPL commands:");
        out.println("  config show                               Print config file path + all entries");
        out.println("  config add type=<t> url=<u> [user=<u>] [token=<t>] [password=<p>]");
        out.println("                                            Add a source entry");
        out.println("  config filter <type> <url> [filter-tokens...]");
        out.println("                                            Set collector-specific filter (no tokens = help)");
        out.println("  config set <type> <url> [field=value ...]  Update fields of an existing entry");
        out.println("  config remove <type> <url>                Remove a source entry");
        out.println("  state show                                Print all checkpoints");
        out.println("  state reset                               Wipe all checkpoint state");
        out.println("  state reset <type> <url>                  Clear checkpoint for one source");
        out.println("  state set-checkpoint <type> <url> <val>   Force-set a checkpoint value");
        out.println("  collect [<type> <url>] [checkpoint <val>] Run collection (optionally filtered)");
        out.println("  agent <claude|ibm-bob>                    Select agent");
        out.println("  status                                    Show current session config");
        out.println("  help                                      Show this help");
        out.println("  exit / quit                               Exit");
        out.println();
        out.println("Agent CLI env vars:");
        out.println("  CLAUDE_COMMAND         claude binary path  (default: claude)");
        out.println("  CLAUDE_MODEL           model override      (optional)");
        out.println("  IBM_BOB_COMMAND        IBM CLI binary      (default: ibmcloud)");
        out.println("  IBM_BOB_ARGS           extra args          (default: ml,text-generation,--input)");
        out.println("  IBM_BOB_STDIN          pipe prompt via stdin instead of arg (default: false)");
        out.println();
        terminal.flush();
    }

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
