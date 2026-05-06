package io.setaicompanion.marshaller;

/**
 * Pluggable marshalling module discovered by {@link java.util.ServiceLoader}.
 *
 * <p>A single provider supplies both a {@link StateMarshaller} and a {@link ConfigMarshaller},
 * ensuring consistent serialisation across state and config stores within the same run.
 */
public interface MarshallerProvider {

    /** Short identifier, e.g. {@code "json"}. */
    String name();

    StateMarshaller getStateMarshaller();

    ConfigMarshaller getConfigMarshaller();
}
