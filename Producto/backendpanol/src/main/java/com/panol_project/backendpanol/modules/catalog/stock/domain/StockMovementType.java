package com.panol_project.backendpanol.modules.catalog.stock.domain;

import java.util.Optional;
import java.util.Locale;
import java.util.Map;

public enum StockMovementType {
    INCREASE_AVAILABLE("increase_available"),
    DECREASE_AVAILABLE("decrease_available"),
    RESERVE("reserve"),
    RELEASE_RESERVE("release_reserve"),
    LOAN("loan"),
    RETURN("return"),
    DAMAGE("damage"),
    REPAIR("repair");

    private static final Map<String, StockMovementType> LEGACY_ALIASES = Map.of(
            "ingreso", INCREASE_AVAILABLE,
            "egreso", DECREASE_AVAILABLE,
            "ajuste", DECREASE_AVAILABLE
    );

    private final String literal;

    StockMovementType(String literal) {
        this.literal = literal;
    }

    public String literal() {
        return literal;
    }

    public static Optional<StockMovementType> fromLiteral(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);

        if (LEGACY_ALIASES.containsKey(normalized)) {
            return Optional.of(LEGACY_ALIASES.get(normalized));
        }

        for (StockMovementType value : values()) {
            if (value.literal.equals(normalized)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
}
