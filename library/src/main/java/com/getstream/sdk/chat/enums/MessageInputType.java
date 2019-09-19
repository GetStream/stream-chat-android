package com.getstream.sdk.chat.enums;

public enum  MessageInputType {
    EDIT_MESSAGE("Edit a message"),
    ADD_FILE("Add a file"),
    UPLOAD_MEDIA("Select your photo or video"),
    UPLOAD_FILE("Select your file"),
    COMMAND("Commands matching"),
    MENTION("Searching for people");

    public final String label;

    MessageInputType(String label) {
        this.label = label;
    }
}
