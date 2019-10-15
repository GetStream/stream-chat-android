package com.getstream.sdk.chat.enums;

public enum MessageStatus {
    RECEIVED("received"),
    FAILED("failed"),
    SENDING("sending"),
    PENDING("pending");

    public final String label;

    // TODO: some people prefer to avoid enums on android
    // https://android.jlelse.eu/android-performance-avoid-using-enum-on-android-326be0794dc3

    MessageStatus(String label) {
        this.label = label;
    }
}
