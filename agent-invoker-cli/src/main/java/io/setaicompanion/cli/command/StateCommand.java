package io.setaicompanion.cli.command;

import io.setaicompanion.store.StateStore;

public class StateCommand implements Command {

    @Override
    public String name() { return "state"; }

    @Override
    public void execute(String[] parts, CommandContext ctx) {
        if (parts.length < 2) {
            ctx.out.warn("Usage: state show|reset|set-checkpoint ...");
            return;
        }
        StateStore st;
        try {
            st = ctx.loadState();
        } catch (Exception e) {
            return;
        }
        if (st == null) return;

        switch (parts[1]) {
            case "show" -> ctx.out.printState(ctx.stateUri, st.allEntries());

            case "reset" -> {
                if (parts.length >= 4) {
                    st.resetCheckpoint(parts[2], parts[3]);
                    ctx.out.info("Checkpoint cleared for: " + parts[2] + " " + parts[3]);
                } else {
                    st.resetAll();
                    ctx.out.info("All checkpoints cleared.");
                }
                ctx.saveState(st);
            }

            case "set-checkpoint" -> {
                if (parts.length < 5) {
                    ctx.out.warn("Usage: state set-checkpoint <type> <url> <value>");
                    return;
                }
                st.setCheckpoint(parts[2], parts[3], parts[4]);
                ctx.saveState(st);
                ctx.out.info("Checkpoint set for " + parts[2] + " " + parts[3]);
            }

            default -> ctx.out.warn("Unknown state sub-command: " + parts[1]);
        }
    }
}
