package io.setaicompanion.cli.command;

import io.setaicompanion.cli.CompanionCLI;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "agent", description = "Set the agent to use")
public class AgentCommand implements Runnable {

    @ParentCommand CompanionCLI root;
    @Parameters(index = "0", description = "Agent name (e.g. claude, ibm-bob)") String name;

    @Override
    public void run() {
        root.agentName = name;
        root.out.info("Agent set to: " + name);
    }
}
