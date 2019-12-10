package com.getstream.sdk.chat.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class State<T> {

    @NonNull
    public final T data;

    @NonNull
    public final Throwable error;

    public State(T data) {
        this.data = data;
        this.error = null;
    }

    public State(@Nullable Throwable error) {
        this.error = error;
        this.data = null;
    }

    public boolean success() {
        return data != null;
    }

    public boolean error() {
        return error != null;
    }
}
