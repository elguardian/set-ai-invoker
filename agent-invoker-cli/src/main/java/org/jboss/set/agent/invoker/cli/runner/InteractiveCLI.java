package org.jboss.set.agent.invoker.cli.runner;

import org.jboss.set.agent.invoker.cli.CompanionCLI;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.UserInterruptException;
import picocli.CommandLine;
import picocli.shell.jline3.PicocliJLineCompleter;

import java.nio.file.Path;

public class InteractiveCLI {

    private final CompanionCLI cli;
    private final CommandLine  commandLine;

    public InteractiveCLI(CompanionCLI cli, CommandLine commandLine) {
        this.cli         = cli;
        this.commandLine = commandLine;
    }

    public void run() {
        LineReader reader = LineReaderBuilder.builder()
            .terminal(cli.terminal)
            .completer(new PicocliJLineCompleter(commandLine.getCommandSpec()))
            .variable(LineReader.HISTORY_FILE,
                Path.of(System.getProperty("user.home"), ".companion_history").toString())
            .build();

        cli.out.header("Set AI Companion — interactive mode");
        cli.out.info("Type 'help' for commands, 'collect' to run, 'exit' to quit.");

        while (true) {
            try {
                String line = reader.readLine("invoker> ").trim();
                if (line.isEmpty()) continue;
                String first = line.split("\\s+", 2)[0];
                if ("exit".equals(first) || "quit".equals(first)) break;
                ParsedLine pl = reader.getParser().parse(line, 0);
                String[] args = pl.words().toArray(new String[0]);
                commandLine.execute(args);
            } catch (UserInterruptException e) {
                continue;
            } catch (EndOfFileException e) {
                break;
            }
        }

        cli.out.info("Goodbye!");
    }
}
