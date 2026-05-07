package io.setaicompanion.cli.runner;

import io.setaicompanion.AgentInvokerService;
import io.setaicompanion.agent.AgentResponse;
import io.setaicompanion.agent.AgentService;
import io.setaicompanion.cli.CLIOptions;
import io.setaicompanion.cli.CLIParser;
import io.setaicompanion.cli.TerminalOutput;
import io.setaicompanion.cli.command.CollectCommand;
import io.setaicompanion.cli.command.CommandContext;
import io.setaicompanion.cli.command.ConfigCommand;
import io.setaicompanion.collector.Filters;
import io.setaicompanion.store.ConfigStore;
import io.setaicompanion.model.ApplicationEvent;
import io.setaicompanion.model.EventSourceConfig;
import io.setaicompanion.store.StateStore;
import org.jline.terminal.Terminal;

import java.util.ArrayList;
import java.util.List;

public class BatchCLI implements RunnerCLI {

    private final String[]           args;
    private final AgentInvokerService service;

    public BatchCLI(String[] args, AgentInvokerService service) {
        this.args    = args;
        this.service = service;
    }

    @Override
    public void run(Terminal terminal) {
        System.exit(execute(terminal));
    }

    private int execute(Terminal terminal) {
        TerminalOutput out = new TerminalOutput(terminal);

        CLIOptions options;
        try {
            options = CLIParser.parse(args);
        } catch (IllegalArgumentException e) {
            out.error(e.getMessage());
            out.printHelp();
            return 1;
        }

        if (options.help()) {
            out.printHelp();
            return 0;
        }

        CommandContext ctx = new CommandContext(
            service, out, terminal,
            options.configUri(), options.stateUri(),
            options.storeImpl(), options.marshaller(), options.agent());

        ConfigStore config;
        StateStore  state;
        try {
            config = ctx.loadConfig();
            state  = ctx.loadState();
        } catch (Exception e) {
            return 1;
        }
        if (config == null || state == null) return 1;

        // ── Config management ────────────────────────────────────────────────
        if (options.configShow()) {
            out.printConfig(options.configUri(), config.entries());
            return 0;
        }

        if (options.configAdd() != null) {
            CLIOptions.ConfigAddOptions a = options.configAdd();
            config.add(new EventSourceConfig(
                a.eventType(), a.eventUrl(), a.eventUser(),
                a.eventApiToken(), a.eventPassword(), Filters.empty()));
            ctx.saveConfig(config);
            out.info("Added: type=" + a.eventType() + " url=" + a.eventUrl());
            return 0;
        }

        if (!options.configRemove().isEmpty()) {
            String type = options.configRemove().get(0);
            String url  = options.configRemove().get(1);
            if (!config.remove(type, url)) {
                out.warn("Entry not found: " + type + " " + url); return 1;
            }
            ctx.saveConfig(config);
            out.info("Removed: " + type + " " + url);
            return 0;
        }

        if (!options.configFilter().isEmpty()) {
            String type   = options.configFilter().get(0);
            String url    = options.configFilter().get(1);
            List<String> tokens = options.configFilter().subList(2, options.configFilter().size());
            return ConfigCommand.applyFilter(type, url, tokens, config, ctx) ? 0 : 1;
        }

        // ── Collection ───────────────────────────────────────────────────────
        List<EventSourceConfig> sources;
        if (options.collectType() != null) {
            EventSourceConfig src = config.find(options.collectType(), options.collectUrl())
                .orElse(null);
            if (src == null) {
                out.warn("Not found in config: " + options.collectType() + " " + options.collectUrl()
                    + ". Add it first with --config-add.");
                return 1;
            }
            sources = List.of(src);
            if (options.overrideCheckpoint() != null) {
                state.setCheckpoint(options.collectType(), options.collectUrl(),
                    options.overrideCheckpoint());
            }
        } else {
            sources = config.entries();
        }

        if (sources.isEmpty()) {
            out.warn("No sources configured. Add sources with --config-add or interactively.");
            return 1;
        }

        AgentService agent = ctx.findAgent();
        if (agent == null) return 1;

        List<ApplicationEvent> allEvents = new ArrayList<>();
        for (EventSourceConfig src : sources) {
            allEvents.addAll(CollectCommand.collectSource(src, state, ctx));
        }

        if (allEvents.isEmpty()) {
            out.info("No new events since last run.");
            return 0;
        }

        out.header("Processing " + allEvents.size() + " event(s) with " + agent.getName());
        int i = 0;
        for (ApplicationEvent event : allEvents) {
            out.printEvent(++i, allEvents.size(), event);
            try {
                AgentResponse response = agent.process(event,
                    line -> out.agentLine(agent.getName(), line));
                out.printResponse(response);
            } catch (Exception e) {
                out.error("Agent failed on event " + event.eventId() + ": " + e.getMessage());
            }
        }

        out.info("Done.");
        return 0;
    }

}
