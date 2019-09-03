package com.getstream.sdk.chat.enums;

public enum MessageStatus {
    RECEIVED("received"),
    FAILED("failed"),
    SENDING("sending");

    public final String label;

    MessageStatus(String label) {
        this.label = label;
    }
}
