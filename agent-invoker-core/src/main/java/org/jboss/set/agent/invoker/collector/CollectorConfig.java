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

/**
 * Resolved credentials and parameters for a single collector invocation.
 *
 * @param url      base URL of the source (GitHub repo API URL or Jira base URL)
 * @param user     optional username for basic-auth sources
 * @param apiToken bearer / personal-access token
 * @param password optional password for basic-auth sources
 * @param filter   collector-specific filter predicates (see each collector's
 *                 {@link EventCollector#filterHelp()} for supported keys)
 */
public record CollectorConfig(
    String  url,
    String  user,
    String  apiToken,
    String  password,
    Filters filter
) {
    public CollectorConfig {
        filter = filter == null ? Filters.empty() : filter;
    }
}
