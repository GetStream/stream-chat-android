package com.getstream.sdk.chat.enums;

public enum ReactionEmoji {
    like("a"),
    love("a"),
    haha("a"),
    wow("a"),
    sad("a"),
    angry("a");

    private String value;

    ReactionEmoji(final String value) {
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
