package io.setaicompanion.cli.runner;

import io.setaicompanion.AgentInvokerService;
import io.setaicompanion.cli.CLIParser;
import io.setaicompanion.cli.TerminalOutput;
import io.setaicompanion.cli.command.Command;
import io.setaicompanion.cli.command.CommandContext;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;

import java.util.Map;

public class InteractiveCLI implements RunnerCLI {

    private final AgentInvokerService  service;
    private final Map<String, Command> commands;

    public InteractiveCLI(AgentInvokerService service, Map<String, Command> commands) {
        this.service  = service;
        this.commands = commands;
    }

    @Override
    public void run(Terminal terminal) {
        TerminalOutput out = new TerminalOutput(terminal);
        out.header("Set AI Companion — interactive mode");
        out.info("Type 'help' for commands, 'collect' to run, 'exit' to quit.");

        LineReader reader = LineReaderBuilder.builder()
            .terminal(terminal)
            .parser(new DefaultParser())
            .completer(new StringsCompleter(
                "config", "config show", "config add", "config set", "config remove", "config filter",
                "state", "state show", "state reset", "state set-checkpoint",
                "collect", "agent", "marshaller", "store-impl", "status", "help", "exit", "quit"))
            .variable(LineReader.HISTORY_FILE, System.getProperty("user.home") + "/.companion_history")
            .build();

        CommandContext ctx = new CommandContext(
            service, out, terminal,
            CLIParser.parseUri(env("CONFIG_URI", "./companion-config.json")),
            CLIParser.parseUri(env("STATE_URI",  "./companion-state.json")),
            env("STORE_IMPL",  null),
            env("MARSHALLER",  null),
            env("AGENT",       "claude"));

        while (true) {
            String line;
            try {
                line = reader.readLine("companion> ").trim();
            } catch (UserInterruptException | EndOfFileException e) {
                break;
            }
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+");

            if (parts[0].equals("exit") || parts[0].equals("quit")) {
                out.info("Goodbye!");
                return;
            }

            Command cmd = commands.get(parts[0]);
            if (cmd != null) {
                cmd.execute(parts, ctx);
            } else {
                out.warn("Unknown command: " + parts[0] + ". Type 'help'.");
            }
        }

        out.info("Goodbye!");
    }

    private static String env(String name, String defaultValue) {
        String v = System.getenv(name);
        return v != null && !v.isBlank() ? v : defaultValue;
    }
}
