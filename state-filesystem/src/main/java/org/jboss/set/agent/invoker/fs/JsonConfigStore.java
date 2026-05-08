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

package org.jboss.set.agent.invoker.fs;

import org.jboss.set.agent.invoker.store.ConfigStore;
import org.jboss.set.agent.invoker.marshaller.ConfigMarshaller;
import org.jboss.set.agent.invoker.model.AgentConfig;
import org.jboss.set.agent.invoker.model.ConfigData;
import org.jboss.set.agent.invoker.model.EventSourceConfig;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JsonConfigStore implements ConfigStore {

    private final List<EventSourceConfig> entries = new ArrayList<>();
    private AgentConfig agentConfig;
    private URI uri;
    private ConfigMarshaller marshaller;

    @Override
    public String name() {
        return "filesystem";
    }

    @Override
    public boolean supports(URI uri) {
        String scheme = uri.getScheme();
        return scheme == null || "file".equals(scheme);
    }

    @Override
    public void init(ConfigMarshaller marshaller) {
        this.marshaller = marshaller;
    }

    @Override
    public void load(URI uri) throws Exception {
        this.uri = uri;
        entries.clear();
        agentConfig = null;
        Path file = toPath(uri);
        if (Files.exists(file)) {
            try {
                ConfigData data = marshaller.unmarshalConfig(Files.readAllBytes(file));
                agentConfig = data.agent();
                entries.addAll(data.sources());
            } catch (Exception e) {
                Log.LOG.configReadError(file.toString(), e.getMessage());
            }
        }
    }

    @Override
    public AgentConfig agent() { return agentConfig; }

    @Override
    public void setAgent(AgentConfig agent) { this.agentConfig = agent; }

    @Override
    public List<EventSourceConfig> entries() {
        return List.copyOf(entries);
    }

    @Override
    public void add(EventSourceConfig config) {
        entries.add(config);
    }

    @Override
    public boolean set(String eventType, String eventUrl, EventSourceConfig updated) {
        for (int i = 0; i < entries.size(); i++) {
            EventSourceConfig e = entries.get(i);
            if (e.eventType().equals(eventType) && e.eventUrl().equals(eventUrl)) {
                entries.set(i, updated);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean remove(String eventType, String eventUrl) {
        return entries.removeIf(e ->
            e.eventType().equals(eventType) && e.eventUrl().equals(eventUrl));
    }

    @Override
    public Optional<EventSourceConfig> find(String eventType, String eventUrl) {
        return entries.stream()
            .filter(e -> e.eventType().equals(eventType) && e.eventUrl().equals(eventUrl))
            .findFirst();
    }

    @Override
    public void save() throws Exception {
        Path file = toPath(uri);
        Path parent = file.getParent();
        if (parent != null) Files.createDirectories(parent);
        byte[] bytes = marshaller.marshalConfig(new ConfigData(agentConfig, entries));
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.write(tmp, bytes);
        Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    private static Path toPath(URI uri) {
        if (uri.getScheme() == null) {
            return Path.of(uri.toString());
        }
        return Path.of(uri);
    }
}
