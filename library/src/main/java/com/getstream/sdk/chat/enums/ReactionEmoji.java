package com.getstream.sdk.chat.enums;

public enum ReactionEmoji {
    like("👍"),
    love("❤"),
    haha("😂"),
    wow("😲"),
    sad("😔"),
    angry("😠");
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
