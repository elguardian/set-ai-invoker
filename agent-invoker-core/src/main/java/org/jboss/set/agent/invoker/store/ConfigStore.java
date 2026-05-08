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

package org.jboss.set.agent.invoker.store;

import org.jboss.set.agent.invoker.marshaller.ConfigMarshaller;
import org.jboss.set.agent.invoker.model.AgentConfig;
import org.jboss.set.agent.invoker.model.EventSourceConfig;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Pluggable configuration store discovered by {@link java.util.ServiceLoader}.
 *
 * <p>Lifecycle: discover → {@link #supports(URI)} → {@link #init(ConfigMarshaller)} →
 * {@link #load(URI)} → use data methods → {@link #save()}.
 */
public interface ConfigStore {

    /** Short identifier, e.g. {@code "filesystem"} or {@code "github"}. */
    String name();

    /** Returns {@code true} if this implementation can handle the given URI. */
    boolean supports(URI uri);

    /** Sets the marshaller to use for (de)serialisation. Called once after discovery. */
    void init(ConfigMarshaller marshaller);

    /** Initialises this store from {@code uri}. Must be called after {@link #init}. */
    void load(URI uri) throws Exception;

    // ── Agent config ──────────────────────────────────────────────────────────

    AgentConfig agent();

    void setAgent(AgentConfig agent);

    // ── Source entries ────────────────────────────────────────────────────────

    List<EventSourceConfig> entries();

    void add(EventSourceConfig config);

    /** Replace the entry matching (eventType, eventUrl). Returns {@code false} if not found. */
    boolean set(String eventType, String eventUrl, EventSourceConfig updated);

    /** Remove the entry matching (eventType, eventUrl). Returns {@code false} if not found. */
    boolean remove(String eventType, String eventUrl);

    Optional<EventSourceConfig> find(String eventType, String eventUrl);

    // ── Persistence ───────────────────────────────────────────────────────────

    /** Persists to the URI supplied in the last {@link #load(URI)} call. */
    void save() throws Exception;
}
