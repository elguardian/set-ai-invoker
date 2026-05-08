package org.jboss.set.agent.invoker.json;

import org.jboss.set.agent.invoker.marshaller.ConfigMarshaller;
import org.jboss.set.agent.invoker.marshaller.MarshallerProvider;
import org.jboss.set.agent.invoker.marshaller.StateMarshaller;

public class JsonMarshallerProvider implements MarshallerProvider {

    private final JsonStateMarshaller  stateMarshaller  = new JsonStateMarshaller();
    private final JsonConfigMarshaller configMarshaller = new JsonConfigMarshaller();

    @Override
    public String name() {
        return "json";
    }

    @Override
    public StateMarshaller getStateMarshaller() {
        return stateMarshaller;
    }

    @Override
    public ConfigMarshaller getConfigMarshaller() {
        return configMarshaller;
    }
}
