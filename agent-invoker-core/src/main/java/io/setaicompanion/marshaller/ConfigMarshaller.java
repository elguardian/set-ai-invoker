package io.setaicompanion.marshaller;

import io.setaicompanion.model.EventSourceConfig;

import java.util.List;

/**
 * Pluggable config marshaller discovered by {@link java.util.ServiceLoader}.
 * Converts {@link EventSourceConfig} lists to/from a byte representation for storage.
 */
public interface ConfigMarshaller {

    /** Short identifier, e.g. {@code "json"}. */
    String name();

    byte[] marshalConfig(List<EventSourceConfig> entries) throws Exception;

    List<EventSourceConfig> unmarshalConfig(byte[] bytes) throws Exception;
}
