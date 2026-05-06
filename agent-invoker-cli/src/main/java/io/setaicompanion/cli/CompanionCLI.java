package io.setaicompanion.cli;

import io.setaicompanion.AgentInvokerService;
import io.setaicompanion.cli.command.AgentCommand;
import io.setaicompanion.cli.command.CollectCommand;
import io.setaicompanion.cli.command.Command;
import io.setaicompanion.cli.command.ConfigCommand;
import io.setaicompanion.cli.command.HelpCommand;
import io.setaicompanion.cli.command.MarshallerCommand;
import io.setaicompanion.cli.command.StateCommand;
import io.setaicompanion.cli.command.StatusCommand;
import io.setaicompanion.cli.command.StoreImplCommand;
import io.setaicompanion.cli.runner.BatchCLI;
import io.setaicompanion.cli.runner.InteractiveCLI;
import io.setaicompanion.cli.runner.RunnerCLI;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CompanionCLI {

    private final AgentInvokerService  service;
    private final Map<String, Command> commands;

    private CompanionCLI() {
        this.service = AgentInvokerService.load();

        this.commands = new LinkedHashMap<>();
        List.<Command>of(
            new HelpCommand(),
            new StatusCommand(),
            new AgentCommand(),
            new MarshallerCommand(),
            new StoreImplCommand(),
            new ConfigCommand(),
            new StateCommand(),
            new CollectCommand()
        ).forEach(c -> commands.put(c.name(), c));
    }

    public static void main(String[] args) throws Exception {
        new CompanionCLI().run(args);
    }

    private void run(String[] args) throws Exception {
        Terminal terminal = TerminalBuilder.builder().system(true).build();
        RunnerCLI runner = args.length == 0
            ? new InteractiveCLI(service, commands)
            : new BatchCLI(args, service);
        runner.run(terminal);
    }
}
