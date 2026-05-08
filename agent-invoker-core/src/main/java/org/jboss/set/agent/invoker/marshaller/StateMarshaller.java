package org.jboss.set.agent.invoker.marshaller;

import org.jboss.set.agent.invoker.model.StateEntry;

import java.util.List;

/**
 * Pluggable state marshaller discovered by {@link java.util.ServiceLoader}.
 * Converts {@link StateEntry} lists to/from a byte representation for storage.
 */
public interface StateMarshaller {

    /** Short identifier, e.g. {@code "json"}. */
    String name();

    byte[] marshalState(List<StateEntry> entries) throws Exception;

    List<StateEntry> unmarshalState(byte[] bytes) throws Exception;
}
