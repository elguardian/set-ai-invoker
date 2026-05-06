package io.setaicompanion.cli.command;

import io.setaicompanion.agent.AgentResponse;
import io.setaicompanion.agent.AgentService;
import io.setaicompanion.cli.Log;
import io.setaicompanion.collector.CollectorConfig;
import io.setaicompanion.collector.EventCollector;
import io.setaicompanion.collector.EventsCollected;
import io.setaicompanion.store.ConfigStore;
import io.setaicompanion.model.ApplicationEvent;
import io.setaicompanion.model.EventSourceConfig;
import io.setaicompanion.store.StateStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CollectCommand implements Command {

    @Override
    public String name() { return "collect"; }

    @Override
    public void execute(String[] parts, CommandContext ctx) {
        ConfigStore cfg;
        StateStore  state;
        try {
            cfg   = ctx.loadConfig();
            state = ctx.loadState();
        } catch (Exception e) {
            return;
        }
        if (cfg == null || state == null) return;

        AgentService ag = ctx.findAgent();
        if (ag == null) return;

        List<EventSourceConfig> sources;
        if (parts.length >= 3) {
            String cType = parts[1], cUrl = parts[2];
            String overrideCheckpoint = null;
            if (parts.length >= 5 && parts[3].equals("checkpoint")) {
                overrideCheckpoint = parts[4];
            }
            EventSourceConfig src = cfg.find(cType, cUrl)
                .orElse(new EventSourceConfig(cType, cUrl, null,
                    env("API_TOKEN", ""), null, Map.of()));
            if (overrideCheckpoint != null) {
                state.setCheckpoint(cType, cUrl, overrideCheckpoint);
            }
            sources = List.of(src);
        } else {
            sources = cfg.entries();
        }

        if (sources.isEmpty()) {
            ctx.out.warn("No sources. Use 'config add' first.");
            return;
        }

        List<ApplicationEvent> allEvents = new ArrayList<>();
        for (EventSourceConfig src : sources) {
            allEvents.addAll(collectSource(src, state, ctx));
        }

        if (allEvents.isEmpty()) {
            ctx.out.info("No new events.");
            return;
        }

        ctx.out.header("Processing " + allEvents.size() + " event(s) with " + ag.getName());
        int i = 0;
        for (ApplicationEvent event : allEvents) {
            ctx.out.printEvent(++i, allEvents.size(), event);
            try {
                AgentResponse resp = ag.process(event, l -> ctx.out.agentLine(ag.getName(), l));
                ctx.out.printResponse(resp);
            } catch (Exception e) {
                ctx.out.error("Agent error: " + e.getMessage());
            }
        }
    }

    public static List<ApplicationEvent> collectSource(EventSourceConfig src,
                                                        StateStore state,
                                                        CommandContext ctx) {
        EventCollector collector = ctx.findCollector(src.eventType());
        if (collector == null) return List.of();

        CollectorConfig cfg = new CollectorConfig(
            src.eventUrl(), src.resolvedUser(), src.resolvedApiToken(),
            src.resolvedPassword(), src.eventFilter());

        String checkpoint = state.getCheckpoint(src.eventType(), src.eventUrl()).orElse(null);

        ctx.out.header(src.eventType() + " → " + src.eventUrl());
        try {
            EventsCollected result = collector.collect(cfg, checkpoint);
            if (result.nextCheckpoint() != null) {
                state.setCheckpoint(src.eventType(), src.eventUrl(), result.nextCheckpoint());
                state.save();
            }
            ctx.out.info("Found " + result.events().size() + " event(s)");
            return result.events();
        } catch (Exception e) {
            ctx.out.error("Collection failed for " + src.eventUrl() + ": " + e.getMessage());
            Log.LOG.collectionError(e.toString());
            return List.of();
        }
    }

    private static String env(String name, String defaultValue) {
        String v = System.getenv(name);
        return v != null && !v.isBlank() ? v : defaultValue;
    }
}
