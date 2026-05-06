package io.setaicompanion.cli.command;

public class HelpCommand implements Command {

    @Override
    public String name() { return "help"; }

    @Override
    public void execute(String[] parts, CommandContext ctx) {
        ctx.out.printHelp();
    }
}
