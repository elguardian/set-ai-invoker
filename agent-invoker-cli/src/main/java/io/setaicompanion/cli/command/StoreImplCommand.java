package io.setaicompanion.cli.command;

import io.setaicompanion.store.StoreProvider;

public class StoreImplCommand implements Command {

    @Override
    public String name() { return "store-impl"; }

    @Override
    public void execute(String[] parts, CommandContext ctx) {
        if (parts.length < 2) {
            ctx.out.warn("Usage: store-impl <name>   (available: "
                + ctx.storeProviders.stream().map(StoreProvider::name).toList() + ")");
            return;
        }
        ctx.storeImpl = parts[1];
        ctx.out.info("Store set to: " + parts[1]);
    }
}
