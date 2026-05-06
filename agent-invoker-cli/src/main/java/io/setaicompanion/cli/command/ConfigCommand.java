package io.setaicompanion.cli.command;

import io.setaicompanion.collector.EventCollector;
import io.setaicompanion.store.ConfigStore;
import io.setaicompanion.model.EventSourceConfig;

import java.util.List;
import java.util.Map;

public class ConfigCommand implements Command {

    @Override
    public String name() { return "config"; }

    @Override
    public void execute(String[] parts, CommandContext ctx) {
        if (parts.length < 2) {
            ctx.out.warn("Usage: config show|add|set|remove|filter ...");
            return;
        }
        ConfigStore cfg;
        try {
            cfg = ctx.loadConfig();
        } catch (Exception e) {
            return;
        }
        if (cfg == null) return;

        switch (parts[1]) {
            case "show"   -> ctx.out.printConfig(ctx.configUri, cfg.entries());
            case "add"    -> add(parts, cfg, ctx);
            case "set"    -> set(parts, cfg, ctx);
            case "remove" -> remove(parts, cfg, ctx);
            case "filter" -> {
                if (parts.length < 4) {
                    ctx.out.warn("Usage: config filter <type> <url> [filter-tokens...]");
                    ctx.collectors.forEach(c -> ctx.out.info("[" + c.getType() + "] " + c.filterHelp()));
                    return;
                }
                applyFilter(parts[2], parts[3], List.of(parts).subList(4, parts.length), cfg, ctx);
            }
            default -> ctx.out.warn("Unknown config sub-command: " + parts[1]);
        }
    }

    private void add(String[] parts, ConfigStore cfg, CommandContext ctx) {
        String type = null, url = null, user = null, token = null, password = null;
        for (int j = 2; j < parts.length; j++) {
            String tok = parts[j];
            if      (tok.startsWith("type="))      type     = tok.substring(5);
            else if (tok.startsWith("url="))       url      = tok.substring(4);
            else if (tok.startsWith("user="))      user     = tok.substring(5);
            else if (tok.startsWith("token="))     token    = tok.substring(6);
            else if (tok.startsWith("api-token=")) token    = tok.substring(10);
            else if (tok.startsWith("password="))  password = tok.substring(9);
            else ctx.out.warn("Unknown field: " + tok);
        }
        if (type == null || url == null) {
            ctx.out.warn("Usage: config add type=<type> url=<url> [user=<u>] [token=<t>] ...");
            return;
        }
        cfg.add(new EventSourceConfig(type, url, user, token, password, Map.of()));
        ctx.saveConfig(cfg);
        ctx.out.info("Added: " + type + " " + url);
    }

    private void set(String[] parts, ConfigStore cfg, CommandContext ctx) {
        if (parts.length < 4) {
            ctx.out.warn("Usage: config set <type> <url> [field=value ...]");
            return;
        }
        String sType = parts[2], sUrl = parts[3];
        EventSourceConfig existing = cfg.find(sType, sUrl).orElse(null);
        if (existing == null) { ctx.out.warn("Not found: " + sType + " " + sUrl); return; }
        String user = existing.eventUser(), token = existing.eventApiToken(),
               password = existing.eventPassword();
        for (int j = 4; j < parts.length; j++) {
            String tok = parts[j];
            if      (tok.startsWith("user="))      user     = tok.substring(5);
            else if (tok.startsWith("token="))     token    = tok.substring(6);
            else if (tok.startsWith("api-token=")) token    = tok.substring(10);
            else if (tok.startsWith("password="))  password = tok.substring(9);
            else if (tok.startsWith("url="))       sUrl     = tok.substring(4);
            else ctx.out.warn("Unknown field: " + tok);
        }
        cfg.set(sType, parts[3],
            new EventSourceConfig(sType, sUrl, user, token, password, existing.eventFilter()));
        ctx.saveConfig(cfg);
        ctx.out.info("Updated: " + sType + " " + parts[3]);
    }

    private void remove(String[] parts, ConfigStore cfg, CommandContext ctx) {
        if (parts.length < 4) {
            ctx.out.warn("Usage: config remove <type> <url>");
            return;
        }
        if (!cfg.remove(parts[2], parts[3])) {
            ctx.out.warn("Not found: " + parts[2] + " " + parts[3]);
            return;
        }
        ctx.saveConfig(cfg);
        ctx.out.info("Removed: " + parts[2] + " " + parts[3]);
    }

    /**
     * Applies a collector-specific filter to a config entry.
     * Returns {@code true} on success (or when printing filter help), {@code false} on error.
     */
    public static boolean applyFilter(String type, String url, List<String> tokens,
                                      ConfigStore cfg, CommandContext ctx) {
        EventCollector collector = ctx.findCollector(type);
        if (collector == null) return false;

        if (tokens.isEmpty()) {
            ctx.out.info("Filter help for '" + type + "': " + collector.filterHelp());
            return true;
        }

        Map<String, Object> filter = collector.parseFilter(tokens);
        EventSourceConfig existing = cfg.find(type, url).orElse(null);
        if (existing == null) {
            ctx.out.warn("No config entry for " + type + " " + url
                + ". Add it first with --config-add or 'config add'.");
            return false;
        }
        cfg.set(type, url, new EventSourceConfig(
            existing.eventType(), existing.eventUrl(), existing.eventUser(),
            existing.eventApiToken(), existing.eventPassword(), filter));
        ctx.saveConfig(cfg);
        ctx.out.info("Filter updated for " + type + " " + url + ": " + filter);
        return true;
    }
}
