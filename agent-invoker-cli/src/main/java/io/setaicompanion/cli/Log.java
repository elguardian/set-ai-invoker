package io.setaicompanion.cli;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "SETAIC")
public interface Log extends BasicLogger {

    Log LOG = Logger.getMessageLogger(Log.class, "io.setaicompanion.cli");

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 1, value = "Collection error: %s")
    void collectionError(String message);
}
