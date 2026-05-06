package io.setaicompanion.cli;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class CLIParser {

    private CLIParser() {}

    public static CLIOptions parse(String[] args) {
        URI configUri  = parseUri(env("CONFIG_URI", "./companion-config.json"));
        URI stateUri   = parseUri(env("STATE_URI",  "./companion-state.json"));
        String storeImpl    = env("STORE_IMPL", null);
        String marshaller   = env("MARSHALLER", null);
        String agent      = env("AGENT", "claude");
        boolean help       = false;
        boolean configShow = false;

        // --config-add fields
        String addType = null, addUrl = null, addUser = null, addToken = null, addPassword = null;

        // --config-remove [type url]
        List<String> configRemove = new ArrayList<>();

        // --config-filter [type url tokens...]
        List<String> configFilter = new ArrayList<>();

        // --collect [type url]
        String collectType = null, collectUrl = null, overrideCheckpoint = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--config-uri"     -> { configUri   = parseUri(requireNext(args, i)); i++; }
                case "--state-uri"      -> { stateUri    = parseUri(requireNext(args, i)); i++; }
                case "--store-impl"     -> { storeImpl   = requireNext(args, i); i++; }
                case "--marshaller"     -> { marshaller  = requireNext(args, i); i++; }
                case "--agent"          -> { agent       = requireNext(args, i); i++; }
                case "--help", "-h"     -> help       = true;
                case "--config-show"    -> configShow = true;

                case "--config-add" -> {
                    i++;
                    while (i < args.length && !args[i].startsWith("--")) {
                        String tok = args[i++];
                        if      (tok.startsWith("type="))           addType     = tok.substring(5);
                        else if (tok.startsWith("url="))            addUrl      = tok.substring(4);
                        else if (tok.startsWith("user="))           addUser     = tok.substring(5);
                        else if (tok.startsWith("token="))          addToken    = tok.substring(6);
                        else if (tok.startsWith("api-token="))      addToken    = tok.substring(10);
                        else if (tok.startsWith("password="))       addPassword = tok.substring(9);
                        else throw new IllegalArgumentException("Unknown --config-add field: " + tok);
                    }
                    i--;
                    if (addType == null || addUrl == null) {
                        throw new IllegalArgumentException("--config-add requires at least type=<type> url=<url>");
                    }
                }

                case "--config-remove" -> {
                    configRemove.add(requireNext(args, i)); i++;
                    configRemove.add(requireNext(args, i)); i++;
                }

                case "--config-filter" -> {
                    configFilter.add(requireNext(args, i)); i++;   // type
                    configFilter.add(requireNext(args, i)); i++;   // url
                    // consume remaining non-flag tokens as filter tokens
                    while (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                        i++;
                        configFilter.add(args[i]);
                    }
                }

                case "--collect" -> {
                    collectType = requireNext(args, i); i++;
                    collectUrl  = requireNext(args, i); i++;
                }

                case "--override-checkpoint" -> { overrideCheckpoint = requireNext(args, i); i++; }

                default -> throw new IllegalArgumentException("Unknown argument: " + args[i]);
            }
        }

        CLIOptions.ConfigAddOptions addOpts = addType != null
            ? new CLIOptions.ConfigAddOptions(addType, addUrl, addUser, addToken, addPassword)
            : null;

        return new CLIOptions(configUri, stateUri, storeImpl, marshaller, agent, help,
            configShow, addOpts, List.copyOf(configRemove), List.copyOf(configFilter),
            collectType, collectUrl, overrideCheckpoint);
    }

    /** If the string already has a URI scheme use it as-is; otherwise treat as a local path. */
    public static URI parseUri(String value) {
        if (value.contains("://") || value.startsWith("file:")) {
            return URI.create(value);
        }
        return Path.of(value).toAbsolutePath().toUri();
    }

    private static String requireNext(String[] args, int i) {
        if (i + 1 >= args.length) {
            throw new IllegalArgumentException("Expected value after " + args[i]);
        }
        return args[i + 1];
    }

    private static String env(String name, String defaultValue) {
        String val = System.getenv(name);
        return val != null && !val.isBlank() ? val : defaultValue;
    }
}
