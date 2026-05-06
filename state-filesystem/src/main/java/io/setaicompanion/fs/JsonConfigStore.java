package io.setaicompanion.fs;

import io.setaicompanion.store.ConfigStore;
import io.setaicompanion.marshaller.ConfigMarshaller;
import io.setaicompanion.model.EventSourceConfig;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JsonConfigStore implements ConfigStore {

    private final List<EventSourceConfig> entries = new ArrayList<>();
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
        Path file = toPath(uri);
        if (Files.exists(file)) {
            try {
                entries.addAll(marshaller.unmarshalConfig(Files.readAllBytes(file)));
            } catch (Exception e) {
                Log.LOG.configReadError(file.toString(), e.getMessage());
            }
        }
    }

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
        byte[] bytes = marshaller.marshalConfig(entries);
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
