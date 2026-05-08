/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.set.agent.invoker.jira;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "SETAICJ")
interface Log extends BasicLogger {

    Log LOG = Logger.getMessageLogger(Log.class, "org.jboss.set.agent.invoker.jira");

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 1, value = "Jira API returned %d for %s")
    void apiError(int status, String url);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 2, value = "Jira: no updated issues since checkpoint")
    void noNewIssues();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 3, value = "Jira issue collected: %s — %s")
    void issueCollected(String issueKey, String summary);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 5, value = "Cannot parse Jira timestamp: %s")
    void timestampParseError(String value);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 6, value = "Jira: %d issue(s) collected from %s")
    void collectionComplete(int count, String url);
}
