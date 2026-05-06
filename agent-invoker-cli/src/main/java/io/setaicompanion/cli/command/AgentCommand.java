package io.setaicompanion.cli.command;

public class AgentCommand implements Command {

    @Override
    public String name() { return "agent"; }

    @Override
    public void execute(String[] parts, CommandContext ctx) {
        if (parts.length < 2) {
            ctx.out.warn("Usage: agent <claude|ibm-bob>");
            return;
        }
        ctx.agentName = parts[1];
        ctx.out.info("Agent set to: " + ctx.agentName);
    }
}
