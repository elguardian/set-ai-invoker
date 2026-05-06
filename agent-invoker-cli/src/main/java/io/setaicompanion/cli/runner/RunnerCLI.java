package io.setaicompanion.cli.runner;

import org.jline.terminal.Terminal;

public interface RunnerCLI {

    void run(Terminal terminal) throws Exception;
}
