package io.setaicompanion.cli.command;

import io.setaicompanion.agent.AgentResponse;
import io.setaicompanion.agent.AgentService;
import io.setaicompanion.cli.CompanionCLI;
import io.setaicompanion.cli.Log;
import io.setaicompanion.collector.CollectorConfig;
import io.setaicompanion.collector.EventCollector;
import io.setaicompanion.collector.EventsCollected;
import io.setaicompanion.model.ApplicationEvent;
import io.setaicompanion.model.EventSourceConfig;
import io.setaicompanion.store.ConfigStore;
import io.setaicompanion.store.StateStore;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.util.ArrayList;
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

        AgentService agent = root.findAgent();
        if (agent == null) return;

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

        List<ApplicationEvent> allEvents = new ArrayList<>();
        for (EventSourceConfig src : sources) {
            allEvents.addAll(collectSource(src, state, root));
        }

        if (allEvents.isEmpty()) {
            root.out.info("No new events.");
            return;
        }

        root.out.header("Processing " + allEvents.size() + " event(s) with " + agent.getName());
        int i = 0;
        for (ApplicationEvent event : allEvents) {
            root.out.printEvent(++i, allEvents.size(), event);
            try {
                AgentResponse resp = agent.process(event, l -> root.out.agentLine(agent.getName(), l));
                root.out.printResponse(resp);
            } catch (Exception e) {
                root.out.error("Agent error: " + e.getMessage());
            }
        }
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
