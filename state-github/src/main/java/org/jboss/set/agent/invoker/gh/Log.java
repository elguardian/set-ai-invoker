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

package org.jboss.set.agent.invoker.gh;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "SETAISG")
interface Log extends BasicLogger {

    Log LOG = Logger.getMessageLogger(Log.class, "org.jboss.set.agent.invoker.gh");

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
