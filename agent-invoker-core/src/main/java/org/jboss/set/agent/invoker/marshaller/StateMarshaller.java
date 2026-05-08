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
