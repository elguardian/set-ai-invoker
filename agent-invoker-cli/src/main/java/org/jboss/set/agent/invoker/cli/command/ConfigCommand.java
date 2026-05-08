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

import org.jboss.set.agent.invoker.cli.CompanionCLI;
import org.jboss.set.agent.invoker.collector.EventCollector;
import org.jboss.set.agent.invoker.collector.FilterParser;
import org.jboss.set.agent.invoker.collector.Filters;
import org.jboss.set.agent.invoker.model.AgentConfig;
import org.jboss.set.agent.invoker.model.EventSourceConfig;
import org.jboss.set.agent.invoker.store.ConfigStore;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

import java.util.List;

@Command(
    name = "config",
    subcommands = {
        ConfigCommand.Show.class,
        ConfigCommand.Add.class,
        ConfigCommand.Set.class,
        ConfigCommand.Remove.class,
        ConfigCommand.Filter.class,
        ConfigCommand.Agent.class
    },
    description = "Manage event source configuration"
)
public class ConfigCommand implements Runnable {

    @ParentCommand CompanionCLI root;
    @Spec CommandSpec spec;

    @Override
    public void run() { spec.commandLine().usage(root.out.writer()); }

    @Command(name = "show", description = "Print all configured sources")
    static class Show implements Runnable {
        @ParentCommand ConfigCommand parent;

        @Override
        public void run() {
            ConfigStore cfg = parent.root.loadConfig();
            if (cfg == null) return;
            parent.root.out.printConfig(parent.root.configUri, cfg.entries());
        }
    }

    @Command(name = "add", description = "Add an event source")
    static class Add implements Runnable {
        @ParentCommand ConfigCommand parent;
        @Option(names = {"-t", "--type"}, required = true, description = "Event type") String type;
        @Option(names = {"-u", "--url"},  required = true, description = "Source URL")  String url;
        @Option(names = "--user",                          description = "Username")     String user;
        @Option(names = {"--token", "--api-token"},        description = "API token")   String token;
        @Option(names = "--password",                      description = "Password")    String password;

        @Override
        public void run() {
            CompanionCLI root = parent.root;
            ConfigStore cfg = root.loadConfig();
            if (cfg == null) return;
            cfg.add(new EventSourceConfig(type, url, user, token, password, Filters.empty()));
            root.saveConfig(cfg);
            root.out.info("Added: " + type + " " + url);
        }
    }

    @Command(name = "set", description = "Update fields of an existing source")
    static class Set implements Runnable {
        @ParentCommand ConfigCommand parent;
        @Parameters(index = "0", description = "Event type") String type;
        @Parameters(index = "1", description = "Source URL")  String url;
        @Option(names = "--user",                   description = "Username")   String user;
        @Option(names = {"--token", "--api-token"}, description = "API token") String token;
        @Option(names = "--password",               description = "Password")  String password;
        @Option(names = "--url",                    description = "New URL")   String newUrl;

        @Override
        public void run() {
            CompanionCLI root = parent.root;
            ConfigStore cfg = root.loadConfig();
            if (cfg == null) return;
            EventSourceConfig existing = cfg.find(type, url).orElse(null);
            if (existing == null) { root.out.warn("Not found: " + type + " " + url); return; }
            String u  = user     != null ? user     : existing.eventUser();
            String t  = token    != null ? token    : existing.eventApiToken();
            String p  = password != null ? password : existing.eventPassword();
            String nu = newUrl   != null ? newUrl   : existing.eventUrl();
            cfg.set(type, url, new EventSourceConfig(type, nu, u, t, p, existing.eventFilter()));
            root.saveConfig(cfg);
            root.out.info("Updated: " + type + " " + url);
        }
    }

    @Command(name = "remove", description = "Remove an event source")
    static class Remove implements Runnable {
        @ParentCommand ConfigCommand parent;
        @Parameters(index = "0", description = "Event type") String type;
        @Parameters(index = "1", description = "Source URL")  String url;

        @Override
        public void run() {
            CompanionCLI root = parent.root;
            ConfigStore cfg = root.loadConfig();
            if (cfg == null) return;
            if (!cfg.remove(type, url)) {
                root.out.warn("Not found: " + type + " " + url);
                return;
            }
            root.saveConfig(cfg);
            root.out.info("Removed: " + type + " " + url);
        }
    }

    @Command(name = "filter", description = "Set collector-specific filter for a source")
    static class Filter implements Runnable {
        @ParentCommand ConfigCommand parent;
        @Parameters(index = "0", description = "Event type") String type;
        @Parameters(index = "1", description = "Source URL")  String url;
        @Parameters(index = "2..*", arity = "0..*", description = "Filter tokens (e.g. project=PROJ)")
        List<String> tokens = List.of();

        @Override
        public void run() {
            CompanionCLI root = parent.root;
            EventCollector collector = root.findCollector(type);
            if (collector == null) return;
            if (tokens.isEmpty()) {
                root.out.info("Filter help for '" + type + "': " + collector.filterHelp());
                return;
            }
            ConfigStore cfg = root.loadConfig();
            if (cfg == null) return;
            Filters filters = FilterParser.parse(collector.getFilterKeysSupported(), tokens);
            EventSourceConfig existing = cfg.find(type, url).orElse(null);
            if (existing == null) {
                root.out.warn("No config entry for " + type + " " + url + ". Add it first.");
                return;
            }
            cfg.set(type, url, new EventSourceConfig(
                existing.eventType(), existing.eventUrl(), existing.eventUser(),
                existing.eventApiToken(), existing.eventPassword(), filters));
            root.saveConfig(cfg);
            root.out.info("Filter updated for " + type + " " + url + ": " + filters);
        }
    }

    @Command(
        name = "agent",
        subcommands = { Agent.Show.class, Agent.Set.class },
        description = "Manage the agent configuration for this config store"
    )
    static class Agent implements Runnable {
        @ParentCommand ConfigCommand parent;
        @Spec CommandSpec spec;

        @Override
        public void run() { spec.commandLine().usage(parent.root.out.writer()); }

        @Command(name = "show", description = "Print the current agent configuration")
        static class Show implements Runnable {
            @ParentCommand Agent agent;

            @Override
            public void run() {
                ConfigStore cfg = agent.parent.root.loadConfig();
                if (cfg == null) return;
                AgentConfig ac = cfg.agent();
                if (ac == null) {
                    agent.parent.root.out.info("No agent configured.");
                } else {
                    agent.parent.root.out.info("implementation: " + ac.implementation());
                    agent.parent.root.out.info("prompt        : " + ac.prompt());
                }
            }
        }

        @Command(name = "set", description = "Set the agent configuration")
        static class Set implements Runnable {
            @ParentCommand Agent agentCmd;
            @Option(names = "--impl",   description = "Agent implementation name") String impl;
            @Option(names = "--prompt", description = "Prompt to pass to the agent") String prompt;

            @Override
            public void run() {
                CompanionCLI root = agentCmd.parent.root;
                ConfigStore cfg = root.loadConfig();
                if (cfg == null) return;
                AgentConfig existing = cfg.agent();
                String effectiveImpl   = impl   != null ? impl   : (existing != null ? existing.implementation() : null);
                String effectivePrompt = prompt != null ? prompt : (existing != null ? existing.prompt()         : null);
                cfg.setAgent(new AgentConfig(effectiveImpl, effectivePrompt));
                root.saveConfig(cfg);
                root.out.info("Agent updated: impl=" + effectiveImpl + " prompt=" + effectivePrompt);
            }
        }
    }
}
