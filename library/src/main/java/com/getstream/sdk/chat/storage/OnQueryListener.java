package com.getstream.sdk.chat.storage;

public interface OnQueryListener<T> {
    void onSuccess(T object);

    void onFailure(Exception e);
}
