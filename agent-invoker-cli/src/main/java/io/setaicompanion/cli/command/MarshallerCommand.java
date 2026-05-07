package io.setaicompanion.cli.command;

import io.setaicompanion.cli.CompanionCLI;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "marshaller", description = "Set the marshaller implementation")
public class MarshallerCommand implements Runnable {

    @ParentCommand CompanionCLI root;
    @Parameters(index = "0", description = "Marshaller name") String name;

    @Override
    public void run() {
        root.marshallerImpl = name;
        root.out.info("Marshaller set to: " + name);
    }
}
