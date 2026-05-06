package io.setaicompanion.cli.command;

public class StatusCommand implements Command {

    @Override
    public String name() { return "status"; }

    @Override
    public void execute(String[] parts, CommandContext ctx) {
        ctx.out.header("Current session");
        ctx.terminal.writer().println("  Config URI    : " + ctx.configUri);
        ctx.terminal.writer().println("  State URI     : " + ctx.stateUri);
        ctx.terminal.writer().println("  Store         : " + (ctx.storeImpl      != null ? ctx.storeImpl      : "(auto)"));
        ctx.terminal.writer().println("  Marshaller    : " + (ctx.marshallerImpl != null ? ctx.marshallerImpl : "(auto)"));
        ctx.terminal.writer().println("  Agent         : " + ctx.agentName);
        ctx.terminal.flush();
    }
}
