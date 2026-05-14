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

package org.jboss.set.agent.invoker.cli;

import org.jboss.set.agent.invoker.AgentInvokerService;
import org.jboss.set.agent.invoker.agent.AgentService;
import org.jboss.set.agent.invoker.cli.command.AgentCommand;
import org.jboss.set.agent.invoker.cli.command.CollectCommand;
import org.jboss.set.agent.invoker.cli.command.ConfigCommand;
import org.jboss.set.agent.invoker.cli.command.StateCommand;
import org.jboss.set.agent.invoker.cli.command.StatusCommand;
import org.jboss.set.agent.invoker.cli.runner.InteractiveCLI;
import org.jboss.set.agent.invoker.collector.EventCollector;
import org.jboss.set.agent.invoker.marshaller.MarshallerProvider;
import org.jboss.set.agent.invoker.store.ConfigStore;
import org.jboss.set.agent.invoker.store.StateStore;
import org.jboss.set.agent.invoker.store.StoreProvider;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import java.net.URI;
import java.nio.file.Path;
import java.util.stream.Collectors;

@Command(
    name = "companion",
    subcommands = {
        ConfigCommand.class, StateCommand.class, CollectCommand.class,
        AgentCommand.class, StatusCommand.class, CommandLine.HelpCommand.class
    },
    mixinStandardHelpOptions = true,
    description = "Set AI Companion — event collection and AI processing"
)
public class CompanionCLI implements Runnable {

    // @Option fields — nullable, applied to session state if non-null, reset to null each parse
    @Option(names = "--store",      description = "Store implementation name") String storeOpt;
    @Option(names = "--marshaller", description = "Marshaller implementation name") String marshallerOpt;
    @Option(names = "--agent",      description = "Agent name") String agentOpt;
    @Option(names = "--store-uri",
        description = "Base storage folder (filesystem path or GitHub repo URL + path)")
    String storeUriOpt;
    @Option(names = "--verbose", description = "Stream agent events to the terminal") boolean verboseOpt;

    // Session state — NOT @Option, set once at startup and never reset by Picocli
    public URI     configUri;
    public URI     stateUri;
    public String  storeImpl;
    public String  marshallerImpl;
    public String  agentName;
    public boolean verbose;

    // Immutable infrastructure
    public final AgentInvokerService service;
    public final TerminalOutput      out;
    public final Terminal            terminal;

    @Spec CommandSpec spec;

    CompanionCLI(AgentInvokerService service, Terminal terminal) {
        this.service  = service;
        this.terminal = terminal;
        this.out      = new TerminalOutput(terminal);
        this.agentName = env("AGENT", "claude");
    }

    @Override
    public void run() {
        if (storeImpl == null || marshallerImpl == null || configUri == null) {
            out.error("--store, --marshaller, and <uri> are required.");
            spec.commandLine().usage(out.writer());
            return;
        }
        new InteractiveCLI(this, spec.commandLine()).run();
    }

    void applyOptions() {
        if (storeOpt      != null) storeImpl      = storeOpt;
        if (marshallerOpt != null) marshallerImpl  = marshallerOpt;
        if (agentOpt      != null) agentName       = agentOpt;
        if (verboseOpt)            verbose         = true;
        if (storeUriOpt != null) {
            String base = storeUriOpt.endsWith("/") ? storeUriOpt : storeUriOpt + "/";
            configUri = parseUri(base + "companion-config.json");
            stateUri  = parseUri(base + "companion-state.json");
        }
    }

    // ── Resolution helpers ─────────────────────────────────────────────────────

    MarshallerProvider resolveMarshaller() {
        if (marshallerImpl == null) {
            out.error("Missing required option: '--marshaller'");
            return null;
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
        if (storeImpl == null) {
            out.error("Missing required option: '--store'");
            return null;
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
        if (configUri == null) {
            out.error("Missing required argument: <uri>");
            return null;
        }
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
        if (stateUri == null) {
            out.error("Missing required argument: <uri>");
            return null;
        }
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
        return findAgent(agentName);
    }

    public AgentService findAgent(String name) {
        return service.agents().stream()
            .filter(a -> a.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElseGet(() -> {
                out.error("No AgentService found with name '" + name + "'. Available: "
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
        System.exit(buildCommandLine(cli).execute(args));
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
