package io.setaicompanion.cli.command;

import io.setaicompanion.marshaller.MarshallerProvider;

public class MarshallerCommand implements Command {

    @Override
    public String name() { return "marshaller"; }

    @Override
    public void execute(String[] parts, CommandContext ctx) {
        if (parts.length < 2) {
            ctx.out.warn("Usage: marshaller <name>   (available: "
                + ctx.marshallerProviders.stream().map(MarshallerProvider::name).toList() + ")");
            return;
        }
        ctx.marshallerImpl = parts[1];
        ctx.out.info("Marshaller set to: " + parts[1]);
    }
}
