package org.jboss.set.agent.invoker.fs;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "SETAISF")
interface Log extends BasicLogger {

    Log LOG = Logger.getMessageLogger(Log.class, "org.jboss.set.agent.invoker.fs");

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 1, value = "Could not read state file %s - starting fresh: %s")
    void stateReadError(String file, String message);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 2, value = "Could not read config file %s - starting empty: %s")
    void configReadError(String file, String message);
}
