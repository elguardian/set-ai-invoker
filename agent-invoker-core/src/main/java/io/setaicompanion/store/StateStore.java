package io.setaicompanion.store;

import io.setaicompanion.marshaller.StateMarshaller;
import io.setaicompanion.model.StateEntry;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Pluggable checkpoint store discovered by {@link java.util.ServiceLoader}.
 *
 * <p>Lifecycle: discover → {@link #supports(URI)} → {@link #init(StateMarshaller)} →
 * {@link #load(URI)} → use data methods → {@link #save()}.
 */
public interface StateStore {

    /** Short identifier, e.g. {@code "filesystem"} or {@code "github"}. */
    String name();

    /** Returns {@code true} if this implementation can handle the given URI. */
    boolean supports(URI uri);

    /** Sets the marshaller to use for (de)serialisation. Called once after discovery. */
    void init(StateMarshaller marshaller);

    /** Initialises this store from {@code uri}. Must be called after {@link #init}. */
    void load(URI uri) throws Exception;

    // ── Data methods ──────────────────────────────────────────────────────────

    Optional<String> getCheckpoint(String eventType, String eventUrl);

    void setCheckpoint(String eventType, String eventUrl, String checkpoint);

    void resetCheckpoint(String eventType, String eventUrl);

    void resetAll();

    List<StateEntry> allEntries();

    // ── Persistence ───────────────────────────────────────────────────────────

    /** Persists to the URI supplied in the last {@link #load(URI)} call. */
    void save() throws Exception;
}
