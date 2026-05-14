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

package org.jboss.set.agent.invoker.ibm;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "SETAIAIB")
interface Log extends BasicLogger {

    Log LOG = Logger.getMessageLogger(Log.class, "org.jboss.set.agent.invoker.ibm");

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 1, value = "IBM Bob CLI exited with code %d")
    void nonZeroExit(int exitCode);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 2, value = "Failed to invoke IBM Bob CLI: %s")
    void invocationError(String message);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 3, value = "IBM Bob turn complete: %s")
    void turnComplete(String subtype);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 4, value = "IBM Bob tool use: %s")
    void toolUseEvent(String toolName);
}
