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

package org.jboss.set.agent.invoker.cli.command;

import org.jboss.set.agent.invoker.agent.AgentResponse;
import org.jboss.set.agent.invoker.agent.AgentService;
import org.jboss.set.agent.invoker.cli.CompanionCLI;
import org.jboss.set.agent.invoker.cli.Log;
import org.jboss.set.agent.invoker.collector.CollectorConfig;
import org.jboss.set.agent.invoker.collector.EventCollector;
import org.jboss.set.agent.invoker.collector.EventsCollected;
import org.jboss.set.agent.invoker.model.AgentConfig;
import org.jboss.set.agent.invoker.model.ApplicationEvent;
import org.jboss.set.agent.invoker.model.EventSourceConfig;
import org.jboss.set.agent.invoker.store.ConfigStore;
import org.jboss.set.agent.invoker.store.StateStore;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.util.List;

@Command(name = "collect", description = "Collect events and process with agent")
public class CollectCommand implements Runnable {

    @ParentCommand CompanionCLI root;

    @Parameters(index = "0", arity = "0..1", description = "Event type") String type;
    @Parameters(index = "1", arity = "0..1", description = "Source URL")  String url;
    @Option(names = "--checkpoint", description = "Override checkpoint for this run")
    String checkpointOverride;

    @Override
    public void run() {
        ConfigStore cfg   = root.loadConfig();
        StateStore  state = root.loadState();
        if (cfg == null || state == null) return;

        List<EventSourceConfig> sources;
        if (type != null && url != null) {
            EventSourceConfig src = cfg.find(type, url).orElse(null);
            if (src == null) {
                root.out.warn("Not found in config: " + type + " " + url
                    + ". Add it first with 'config add'.");
                return;
            }
            if (checkpointOverride != null) {
                state.setCheckpoint(type, url, checkpointOverride);
            }
            sources = List.of(src);
        } else {
            sources = cfg.entries();
        }

        if (sources.isEmpty()) {
            root.out.warn("No sources. Use 'config add' first.");
            return;
        }

        AgentConfig agentConfig = cfg.agent();
        AgentService agent = resolveAgent(agentConfig, root);
        if (agent == null) return;
        String prompt = agentConfig != null ? agentConfig.prompt() : null;

        for (EventSourceConfig src : sources) {
            List<ApplicationEvent> events = collectSource(src, state, root);
            if (events.isEmpty()) continue;

            root.out.header("Processing " + events.size() + " event(s) with " + agent.getName());
            int i = 0;
            for (ApplicationEvent event : events) {
                root.out.printEvent(++i, events.size(), event);
                try {
                    AgentResponse resp = agent.process(event, prompt.replace("[event]", event.toString()), l -> root.out.agentLine(agent.getName(), l));
                    root.out.printResponse(resp);
                } catch (Exception e) {
                    root.out.error("Agent error: " + e.getMessage());
                }
            }
        }
    }

    private static AgentService resolveAgent(AgentConfig agentConfig, CompanionCLI root) {
        if (agentConfig != null && agentConfig.implementation() != null) {
            return root.findAgent(agentConfig.implementation());
        }
        return root.findAgent();
    }

    static List<ApplicationEvent> collectSource(EventSourceConfig src, StateStore state, CompanionCLI root) {
        EventCollector collector = root.findCollector(src.eventType());
        if (collector == null) return List.of();

        CollectorConfig cfg = new CollectorConfig(
            src.eventUrl(), src.resolvedUser(), src.resolvedApiToken(),
            src.resolvedPassword(), src.eventFilter());

        String checkpoint = state.getCheckpoint(src.eventType(), src.eventUrl()).orElse(null);

        root.out.header(src.eventType() + " → " + src.eventUrl());
        try {
            EventsCollected result = collector.collect(cfg, checkpoint);
            if (result.nextCheckpoint() != null) {
                state.setCheckpoint(src.eventType(), src.eventUrl(), result.nextCheckpoint());
                state.save();
            }
            root.out.info("Found " + result.events().size() + " event(s)");
            return result.events();
        } catch (Exception e) {
            root.out.error("Collection failed for " + src.eventUrl() + ": " + e.getMessage());
            Log.LOG.collectionError(e.toString());
            return List.of();
        }
    }
}
