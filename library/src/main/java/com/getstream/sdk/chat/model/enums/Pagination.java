package com.getstream.sdk.chat.model.enums;

public enum Pagination {
    greaterThan("id_gt"),
    greaterThanOrEqual("id_gte"),
    lessThan("id_lt"),
    lessThanOrEqual("id_lte");

    private String value;

    Pagination(final String value) {
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
