package io.setaicompanion.cli.command;

import io.setaicompanion.cli.CompanionCLI;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "store", description = "Set the store implementation")
public class StoreCommand implements Runnable {

    @ParentCommand CompanionCLI root;
    @Parameters(index = "0", description = "Store name") String name;

    @Override
    public void run() {
        root.storeImpl = name;
        root.out.info("Store set to: " + name);
    }
}
