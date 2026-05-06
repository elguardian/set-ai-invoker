package io.setaicompanion.jira;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "SETAICJ")
interface Log extends BasicLogger {

    Log LOG = Logger.getMessageLogger(Log.class, "io.setaicompanion.jira");

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 1, value = "Jira API returned %d for %s")
    void apiError(int status, String url);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 2, value = "Jira: no new audit records since checkpoint")
    void noNewRecords();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 3, value = "Jira event: %s - %s: %s -> %s (by %s)")
    void fieldChangeDetected(String issueKey, String fieldName, String oldValue,
                             String newValue, String changedBy);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 4, value = "Unknown Jira filter token: %s")
    void unknownFilterToken(String token);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 5, value = "Cannot parse Jira timestamp: %s")
    void timestampParseError(String value);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 6, value = "Jira: %d field-change event(s) from %s")
    void collectionComplete(int count, String url);
}
