package com.panol_project.backendpanol.modules.catalog.stock.domain;

import java.util.Optional;

public enum StockMovementType {
    INCREASE_AVAILABLE("increase_available"),
    DECREASE_AVAILABLE("decrease_available"),
    RESERVE("reserve"),
    RELEASE_RESERVE("release_reserve"),
    LOAN("loan"),
    RETURN("return"),
    DAMAGE("damage"),
    REPAIR("repair");

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
        String normalized = raw.trim().toLowerCase();
        for (StockMovementType value : values()) {
            if (value.literal.equals(normalized)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
}
