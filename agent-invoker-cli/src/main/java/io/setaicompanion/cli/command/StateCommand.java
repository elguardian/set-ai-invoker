package io.setaicompanion.cli.command;

import io.setaicompanion.cli.CompanionCLI;
import io.setaicompanion.store.StateStore;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

@Command(
    name = "state",
    subcommands = {StateCommand.Show.class, StateCommand.Reset.class, StateCommand.SetCheckpoint.class},
    description = "Manage checkpoint state"
)
public class StateCommand implements Runnable {

    @ParentCommand CompanionCLI root;
    @Spec CommandSpec spec;

    @Override
    public void run() { spec.commandLine().usage(root.out.writer()); }

    @Command(name = "show", description = "Print all checkpoints")
    static class Show implements Runnable {
        @ParentCommand StateCommand parent;

        @Override
        public void run() {
            StateStore state = parent.root.loadState();
            if (state == null) return;
            parent.root.out.printState(parent.root.stateUri, state.allEntries());
        }
    }

    @Command(name = "reset", description = "Clear checkpoints (all, or for a specific source)")
    static class Reset implements Runnable {
        @ParentCommand StateCommand parent;
        @Parameters(index = "0", arity = "0..1", description = "Event type") String type;
        @Parameters(index = "1", arity = "0..1", description = "Source URL")  String url;

        @Override
        public void run() {
            CompanionCLI root = parent.root;
            StateStore state = root.loadState();
            if (state == null) return;
            if (type != null && url != null) {
                state.resetCheckpoint(type, url);
                root.out.info("Checkpoint cleared for: " + type + " " + url);
            } else {
                state.resetAll();
                root.out.info("All checkpoints cleared.");
            }
            root.saveState(state);
        }
    }

    @Command(name = "set-checkpoint", description = "Force-set a checkpoint value")
    static class SetCheckpoint implements Runnable {
        @ParentCommand StateCommand parent;
        @Parameters(index = "0", description = "Event type")       String type;
        @Parameters(index = "1", description = "Source URL")        String url;
        @Parameters(index = "2", description = "Checkpoint value") String value;

        @Override
        public void run() {
            CompanionCLI root = parent.root;
            StateStore state = root.loadState();
            if (state == null) return;
            state.setCheckpoint(type, url, value);
            root.saveState(state);
            root.out.info("Checkpoint set for " + type + " " + url);
        }
    }
}
