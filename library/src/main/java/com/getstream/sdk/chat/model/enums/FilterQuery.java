package com.getstream.sdk.chat.model.enums;

public enum FilterQuery {
    EQUAL("$eq"),
    GREATER("$gt"),
    GREATER_OR_EQUAL("$gte"),
    LESS("$lt"),
    LESS_OR_EQUAL("$lte"),
    NOTEQUAL("$ne"),
    IN("$in"),
    NIN("$nin"),
    AND("$and"),
    OR("$or"),
    NOR("$nor");

    private String value;

    FilterQuery(final String value) {
        this.value = value;
    }

    public String get() {
        return value;
    }

    @Override
    public String toString() {
        return this.get();
    }
}
