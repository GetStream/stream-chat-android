package com.getstream.sdk.chat.model.enums;

public enum Token {
    SERVERSIDE,
    DEVELOPMENT,
    GUEST,
    HARDCODED;

    private String value;

    public void setToken(String value) {
        this.value = value;
    }

    public String getToken() {
        return value;
    }

    @Override
    public String toString() {
        return this.getToken();
    }
}
