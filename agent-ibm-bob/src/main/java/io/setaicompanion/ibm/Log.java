package io.setaicompanion.ibm;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "SETAIAIB")
interface Log extends BasicLogger {

    Log LOG = Logger.getMessageLogger(Log.class, "io.setaicompanion.ibm");

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 1, value = "IBM Bob CLI exited with code %d")
    void nonZeroExit(int exitCode);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 2, value = "Failed to invoke IBM Bob CLI: %s")
    void invocationError(String message);
}
