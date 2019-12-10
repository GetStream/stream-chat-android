package com.getstream.sdk.chat.utils;

public class ClientError extends RuntimeException {

    public ClientError(String message, int code) {
        super(message + ": code: " + code);
    }
}
