package io.setaicompanion.gh;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "SETAISG")
interface Log extends BasicLogger {

    Log LOG = Logger.getMessageLogger(Log.class, "io.setaicompanion.gh");

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 1, value = "State file not found in GitHub (%s/%s:%s/%s) — starting fresh")
    void stateFileNotFound(String owner, String repo, String branch, String path);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 2, value = "Could not parse state from GitHub — starting fresh: %s")
    void stateParseError(String message);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 3, value = "Config file not found in GitHub (%s/%s:%s/%s) — starting empty")
    void configFileNotFound(String owner, String repo, String branch, String path);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 4, value = "Could not parse config from GitHub — starting empty: %s")
    void configParseError(String message);
}
