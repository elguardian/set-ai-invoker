package org.jboss.set.agent.invoker.collector;

/**
 * Comparison operators used in {@link Filter} predicates.
 * <p>
 * {@link #IN} is an internal operator produced by {@link Filters.MultipleValueBuilder} —
 * it is not parsed from token strings.
 */
public enum FilterOperator {

    EQ("="),
    LT("<"),
    GT(">"),
    LTE("<="),
    GTE(">="),
    IN("in");

    private final String symbol;

    FilterOperator(String symbol) {
        this.symbol = symbol;
    }

    /** The token symbol for this operator (e.g. {@code "<="}). */
    public String symbol() {
        return symbol;
    }

    /** Resolves a symbol string to its enum constant. */
    public static FilterOperator fromSymbol(String symbol) {
        for (FilterOperator op : values()) {
            if (op.symbol.equals(symbol)) return op;
        }
        throw new IllegalArgumentException("Unknown operator symbol: " + symbol);
    }
}
