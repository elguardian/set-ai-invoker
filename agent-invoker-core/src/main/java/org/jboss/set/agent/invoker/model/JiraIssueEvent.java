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

package org.jboss.set.agent.invoker.model;

import java.time.Instant;

public record JiraIssueEvent(
    String  eventId,
    Instant timestamp,
    String  issueKey,
    String  summary,
    String  status,
    String  assignee,
    String  priority,
    String  reporter,
    String  pmAck,
    String  devAck,
    String  qeAck,
    String  targetRelease,
    String  affectedVersion
) implements ApplicationEvent {

    @Override
    public String toString() {
        return "Jira Issue " + issueKey + "\n" +
               "Summary: "          + summary         + "\n" +
               "Status: "           + status           + "\n" +
               "Priority: "         + priority         + "\n" +
               "Reporter: "         + reporter         + "\n" +
               "Assignee: "         + assignee         + "\n" +
               "PM Ack: "           + pmAck  + "  Dev Ack: " + devAck + "  QE Ack: " + qeAck + "\n" +
               "Target Release: "   + targetRelease    + "\n" +
               "Affected Version: " + affectedVersion;
    }
}
