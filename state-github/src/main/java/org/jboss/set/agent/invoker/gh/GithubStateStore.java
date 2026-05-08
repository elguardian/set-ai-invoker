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

import org.jboss.set.agent.invoker.marshaller.StateMarshaller;
import org.jboss.set.agent.invoker.model.StateEntry;
import org.jboss.set.agent.invoker.store.StateStore;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GithubStateStore implements StateStore {

    private final GithubApi api = new GithubApi();

    private final Map<String, String> checkpoints = new LinkedHashMap<>();
    private GithubApi.Coords coords;
    private String blobSha;
    private StateMarshaller marshaller;

    @Override
    public String name() {
        return "github";
    }

    @Override
    public boolean supports(URI uri) {
        return "https".equals(uri.getScheme()) && "github.com".equals(uri.getHost());
    }

    @Override
    public void init(StateMarshaller marshaller) {
        this.marshaller = marshaller;
    }

    @Override
    public void load(URI uri) throws Exception {
        this.coords = GithubApi.Coords.from(uri);
        checkpoints.clear();
        blobSha = null;

        GithubApi.FileContent fc = api.get(coords);
        if (fc == null) {
            Log.LOG.stateFileNotFound(coords.owner(), coords.repo(), coords.branch(), coords.path());
            return;
        }

        blobSha = fc.sha();
        try {
            List<StateEntry> entries = marshaller.unmarshalState(fc.content());
            for (StateEntry e : entries) {
                checkpoints.put(key(e.eventType(), e.eventUrl()), e.eventCheckpoint());
            }
        } catch (Exception e) {
            Log.LOG.stateParseError(e.getMessage());
        }
    }

    @Override
    public Optional<String> getCheckpoint(String eventType, String eventUrl) {
        return Optional.ofNullable(checkpoints.get(key(eventType, eventUrl)));
    }

    @Override
    public void setCheckpoint(String eventType, String eventUrl, String checkpoint) {
        checkpoints.put(key(eventType, eventUrl), checkpoint);
    }

    @Override
    public void resetCheckpoint(String eventType, String eventUrl) {
        checkpoints.remove(key(eventType, eventUrl));
    }

    @Override
    public void resetAll() {
        checkpoints.clear();
    }

    @Override
    public List<StateEntry> allEntries() {
        List<StateEntry> entries = new ArrayList<>();
        for (Map.Entry<String, String> e : checkpoints.entrySet()) {
            String[] parts = e.getKey().split("\0", 2);
            entries.add(new StateEntry(parts[0], parts[1], e.getValue()));
        }
        return List.copyOf(entries);
    }

    @Override
    public void save() throws Exception {
        byte[] content = marshaller.marshalState(allEntries());
        api.put(coords, content, blobSha, "chore: update companion state");
    }

    private static String key(String eventType, String eventUrl) {
        return eventType + "\0" + eventUrl;
    }
}
