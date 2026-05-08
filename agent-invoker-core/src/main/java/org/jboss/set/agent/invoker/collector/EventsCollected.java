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

package org.jboss.set.agent.invoker.collector;

import org.jboss.set.agent.invoker.model.ApplicationEvent;

import java.util.List;

/**
 * Result of a single collector run.
 *
 * @param nextCheckpoint opaque string to persist as the new checkpoint, or {@code null} if
 *                       the checkpoint has not changed and should not be updated
 * @param events         ordered list of new application events, oldest first
 */
public record EventsCollected(
    String nextCheckpoint,
    List<ApplicationEvent> events
) {}
