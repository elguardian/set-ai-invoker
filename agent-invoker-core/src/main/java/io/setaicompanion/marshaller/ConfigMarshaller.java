package io.setaicompanion.marshaller;

import io.setaicompanion.model.ConfigData;

/**
 * Pluggable config marshaller discovered by {@link java.util.ServiceLoader}.
 * Converts {@link ConfigData} (agent config + event sources) to/from a byte representation.
 */
public interface ConfigMarshaller {

    /** Short identifier, e.g. {@code "json"}. */
    String name();

    byte[] marshalConfig(ConfigData data) throws Exception;

    ConfigData unmarshalConfig(byte[] bytes) throws Exception;
}
