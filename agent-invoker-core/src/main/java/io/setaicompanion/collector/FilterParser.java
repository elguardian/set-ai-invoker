package io.setaicompanion.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Parses CLI filter tokens (e.g. {@code ["project=FOO", "count>=5"]}) into a {@link Filters}.
 * <p>
 * Each token must have the form {@code key<op>value} where {@code <op>} is one of
 * {@code =}, {@code <}, {@code >}, {@code <=}, {@code >=}.  Operators are matched
 * longest-first so {@code <=} and {@code >=} take precedence over {@code <} and {@code >}.
 * <p>
 * Tokens whose key is not in {@code supportedKeys} are silently skipped (pass an empty
 * list to disable key validation, e.g. when deserialising from storage).
 */
public final class FilterParser {

    private static final List<String> OPERATOR_ORDER = List.of("<=", ">=", "<", ">", "=");

    private FilterParser() {}

    public static Filters parse(List<String> supportedKeys, List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) return Filters.empty();

        Set<String> supported = supportedKeys == null ? Set.of() : Set.copyOf(supportedKeys);
        List<Filter> result   = new ArrayList<>();

        for (String token : tokens) {
            Filter f = parseToken(token);
            if (f == null) continue;
            if (!supported.isEmpty() && !supported.contains(f.key())) continue;
            result.add(f);
        }

        return Filters.of(result);
    }

    /** Parses without key validation — used when loading from storage. */
    public static Filters parseTokens(List<String> tokens) {
        return parse(List.of(), tokens);
    }

    private static Filter parseToken(String token) {
        for (String op : OPERATOR_ORDER) {
            int idx = token.indexOf(op);
            if (idx > 0) {
                String key   = token.substring(0, idx).trim();
                String value = token.substring(idx + op.length()).trim();
                if (!key.isEmpty() && !value.isEmpty()) {
                    return new Filter(key, op, value);
                }
            }
        }
        return null;
    }
}
