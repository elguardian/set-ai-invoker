package io.setaicompanion.cli.command;

public interface Command {

    String name();

    void execute(String[] parts, CommandContext ctx);
}
