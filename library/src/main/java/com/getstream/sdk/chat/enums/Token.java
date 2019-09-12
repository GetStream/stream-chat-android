package com.getstream.sdk.chat.enums;

public enum Token {
    SERVERSIDE,
    DEVELOPMENT,
    GUEST,
    HARDCODED;

    private String value;

    public String getToken() {
        return value;
    }

    public void setToken(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.getToken();
    }
}
