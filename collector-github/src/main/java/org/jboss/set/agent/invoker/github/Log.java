package org.jboss.set.agent.invoker.github;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "SETAICG")
interface Log extends BasicLogger {

    Log LOG = Logger.getMessageLogger(Log.class, "org.jboss.set.agent.invoker.github");

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 1, value = "GitHub: no changes for %s (304 Not Modified)")
    void noChanges(String repoKey);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 2, value = "GitHub API returned %d for %s")
    void apiError(int status, String repoKey);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 3, value = "GitHub PR event: #%d [%s] action=%s by %s")
    void prDetected(int prNumber, String repoKey, String action, String author);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 4, value = "GitHub: %d PR event(s) collected from %s")
    void collectionComplete(int count, String repoKey);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 5, value = "Could not parse GitHub checkpoint, resetting: %s")
    void checkpointParseError(String message);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 6, value = "Could not serialise GitHub checkpoint: %s")
    void checkpointSerialiseError(String message);
}
