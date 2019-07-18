package com.getstream.sdk.chat.model.enums;

public enum FilterQuery {
    equal("$eq"),
    greater("$gt"),
    greaterOrEqual("$gte"),
    less("$lt"),
    lessOrEqual("$lte"),
    notEqual("$ne"),
    in("$in"),
    nin("$nin"),
    and("$and"),
    or("$or"),
    nor("$nor");

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
