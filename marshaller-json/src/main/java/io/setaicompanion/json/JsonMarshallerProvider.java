package io.setaicompanion.json;

import io.setaicompanion.marshaller.ConfigMarshaller;
import io.setaicompanion.marshaller.MarshallerProvider;
import io.setaicompanion.marshaller.StateMarshaller;

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
