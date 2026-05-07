package io.setaicompanion.cli;

import io.setaicompanion.AgentInvokerService;
import io.setaicompanion.agent.AgentService;
import io.setaicompanion.cli.command.AgentCommand;
import io.setaicompanion.cli.command.CollectCommand;
import io.setaicompanion.cli.command.ConfigCommand;
import io.setaicompanion.cli.command.MarshallerCommand;
import io.setaicompanion.cli.command.StateCommand;
import io.setaicompanion.cli.command.StatusCommand;
import io.setaicompanion.cli.command.StoreCommand;
import io.setaicompanion.cli.runner.InteractiveCLI;
import io.setaicompanion.collector.EventCollector;
import io.setaicompanion.marshaller.MarshallerProvider;
import io.setaicompanion.store.ConfigStore;
import io.setaicompanion.store.StateStore;
import io.setaicompanion.store.StoreProvider;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;
import picocli.CommandLine.Spec;

import java.net.URI;
import java.nio.file.Path;
import java.util.stream.Collectors;

@Command(
    name = "companion",
    subcommands = {
        ConfigCommand.class, StateCommand.class, CollectCommand.class,
        AgentCommand.class, MarshallerCommand.class, StoreCommand.class,
        StatusCommand.class, CommandLine.HelpCommand.class
    },
    mixinStandardHelpOptions = true,
    description = "Set AI Companion — event collection and AI processing"
)
public class CompanionCLI implements Runnable {

    // @Option fields — nullable, applied to session state if non-null, reset to null each parse
    @Option(names = "--config-uri", scope = ScopeType.INHERIT, description = "Config URI")
    String configUriOpt;

    @Option(names = "--state-uri", scope = ScopeType.INHERIT, description = "State URI")
    String stateUriOpt;

    @Option(names = "--store", scope = ScopeType.INHERIT, description = "Store implementation")
    String storeOpt;

    @Option(names = "--marshaller", scope = ScopeType.INHERIT, description = "Marshaller implementation")
    String marshallerOpt;

    @Option(names = "--agent", scope = ScopeType.INHERIT, description = "Agent name")
    String agentOpt;

    // Session state — NOT @Option, survives REPL iterations
    public URI    configUri;
    public URI    stateUri;
    public String storeImpl;
    public String marshallerImpl;
    public String agentName;

    // Immutable infrastructure
    public final AgentInvokerService service;
    public final TerminalOutput      out;
    public final Terminal            terminal;

    @Spec CommandSpec spec;

    CompanionCLI(AgentInvokerService service, Terminal terminal) {
        this.service  = service;
        this.terminal = terminal;
        this.out      = new TerminalOutput(terminal);
        this.configUri      = parseUri(env("CONFIG_URI", "./companion-config.json"));
        this.stateUri       = parseUri(env("STATE_URI",  "./companion-state.json"));
        this.storeImpl      = env("STORE_IMPL", null);
        this.marshallerImpl = env("MARSHALLER",  null);
        this.agentName      = env("AGENT",       "claude");
    }

    @Override
    public void run() {
        spec.commandLine().usage(out.writer());
    }

    void applyOptions() {
        if (configUriOpt  != null) configUri      = parseUri(configUriOpt);
        if (stateUriOpt   != null) stateUri        = parseUri(stateUriOpt);
        if (storeOpt      != null) storeImpl       = storeOpt;
        if (marshallerOpt != null) marshallerImpl  = marshallerOpt;
        if (agentOpt      != null) agentName       = agentOpt;
    }

    // ── Resolution helpers ─────────────────────────────────────────────────────

    MarshallerProvider resolveMarshaller() {
        if (service.marshallerProviders().isEmpty()) {
            out.error("No MarshallerProvider implementation found on classpath.");
            return null;
        }
        if (marshallerImpl == null) {
            if (service.marshallerProviders().size() > 1) {
                String names = service.marshallerProviders().stream()
                    .map(MarshallerProvider::name).collect(Collectors.joining(", "));
                out.error("Multiple MarshallerProvider implementations available: " + names
                    + "\n  Specify one with --marshaller <name>");
                return null;
            }
            return service.marshallerProviders().get(0);
        }
        MarshallerProvider found = service.marshallerProviders().stream()
            .filter(m -> m.name().equals(marshallerImpl)).findFirst().orElse(null);
        if (found == null) {
            out.error("No MarshallerProvider named '" + marshallerImpl + "'. Available: "
                + service.marshallerProviders().stream().map(MarshallerProvider::name).toList());
        }
        return found;
    }

    StoreProvider resolveStoreProvider() {
        if (service.storeProviders().isEmpty()) {
            out.error("No StoreProvider implementation found on classpath.");
            return null;
        }
        if (storeImpl == null) {
            if (service.storeProviders().size() > 1) {
                String names = service.storeProviders().stream()
                    .map(StoreProvider::name).collect(Collectors.joining(", "));
                out.error("Multiple StoreProvider implementations available: " + names
                    + "\n  Specify one with --store <name>");
                return null;
            }
            return service.storeProviders().get(0);
        }
        StoreProvider found = service.storeProviders().stream()
            .filter(p -> p.name().equals(storeImpl)).findFirst().orElse(null);
        if (found == null) {
            out.error("No StoreProvider named '" + storeImpl + "'. Available: "
                + service.storeProviders().stream().map(StoreProvider::name).toList());
        }
        return found;
    }

    public ConfigStore loadConfig() {
        MarshallerProvider mp = resolveMarshaller();
        StoreProvider      sp = resolveStoreProvider();
        if (mp == null || sp == null) return null;
        try {
            ConfigStore store = sp.getConfigStore();
            store.init(mp.getConfigMarshaller());
            store.load(configUri);
            return store;
        } catch (Exception e) {
            out.error("Could not load config: " + e.getMessage());
            return null;
        }
    }

    public StateStore loadState() {
        MarshallerProvider mp = resolveMarshaller();
        StoreProvider      sp = resolveStoreProvider();
        if (mp == null || sp == null) return null;
        try {
            StateStore store = sp.getStateStore();
            store.init(mp.getStateMarshaller());
            store.load(stateUri);
            return store;
        } catch (Exception e) {
            out.error("Could not load state: " + e.getMessage());
            return null;
        }
    }

    public EventCollector findCollector(String type) {
        return service.collectors().stream()
            .filter(c -> c.getType().equals(type))
            .findFirst()
            .orElseGet(() -> {
                out.error("No EventCollector found for type '" + type + "'.");
                return null;
            });
    }

    public AgentService findAgent() {
        return service.agents().stream()
            .filter(a -> a.getName().equalsIgnoreCase(agentName))
            .findFirst()
            .orElseGet(() -> {
                out.error("No AgentService found with name '" + agentName + "'. Available: "
                    + service.agents().stream().map(AgentService::getName).toList());
                return null;
            });
    }

    public void saveConfig(ConfigStore cfg) {
        try { cfg.save(); } catch (Exception e) {
            out.error("Could not save config: " + e.getMessage());
        }
    }

    public void saveState(StateStore state) {
        try { state.save(); } catch (Exception e) {
            out.error("Could not save state: " + e.getMessage());
        }
    }

    // ── Entry point ────────────────────────────────────────────────────────────

    public static void main(String[] args) throws Exception {
        Terminal terminal = TerminalBuilder.builder().system(true).build();
        CompanionCLI cli = new CompanionCLI(AgentInvokerService.load(), terminal);
        CommandLine cmd = buildCommandLine(cli);
        if (args.length == 0) {
            new InteractiveCLI(cli, cmd).run();
        } else {
            System.exit(cmd.execute(args));
        }
    }

    static CommandLine buildCommandLine(CompanionCLI cli) {
        return new CommandLine(cli)
            .setExecutionStrategy(pr -> {
                cli.applyOptions();
                return new CommandLine.RunLast().execute(pr);
            })
            .setExecutionExceptionHandler((ex, cl, pr) -> {
                cli.out.error(ex.getMessage());
                return 1;
            });
    }

    // ── Utilities ──────────────────────────────────────────────────────────────

    static URI parseUri(String value) {
        if (value.contains("://") || value.startsWith("file:")) {
            return URI.create(value);
        }
        return Path.of(value).toAbsolutePath().toUri();
    }

    private static String env(String name, String defaultValue) {
        String v = System.getenv(name);
        return v != null && !v.isBlank() ? v : defaultValue;
    }
}
