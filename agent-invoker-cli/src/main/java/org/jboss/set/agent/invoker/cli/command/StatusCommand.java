package org.jboss.set.agent.invoker.cli.command;

import org.jboss.set.agent.invoker.cli.CompanionCLI;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command(name = "status", description = "Show current session configuration")
public class StatusCommand implements Runnable {

    @ParentCommand CompanionCLI root;

    @Override
    public void run() {
        root.out.header("Current session");
        root.terminal.writer().println("  Config URI    : " + root.configUri);
        root.terminal.writer().println("  State URI     : " + root.stateUri);
        root.terminal.writer().println("  Store         : " + (root.storeImpl      != null ? root.storeImpl      : "(auto)"));
        root.terminal.writer().println("  Marshaller    : " + (root.marshallerImpl != null ? root.marshallerImpl : "(auto)"));
        root.terminal.writer().println("  Agent         : " + root.agentName);
        root.terminal.flush();
    }
}
