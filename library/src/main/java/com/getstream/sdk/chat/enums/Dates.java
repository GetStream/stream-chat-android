package com.getstream.sdk.chat.enums;

public enum Dates {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    JUST_NOW("Just Now");

    public final String label;

    Dates(String label) {
        this.label = label;
    }
}
