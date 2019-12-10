package com.getstream.sdk.chat.utils;

public class Observable<T> {

    private final SuccessCallback<T> successCallback;
    private final ErrorCallback errorCallback;

    public Observable(SuccessCallback<T> successCallback, ErrorCallback errorCallback) {

        this.successCallback = successCallback;
        this.errorCallback = errorCallback;
    }

    public Observable<T> async(Runnable runnable) {

        try {
            runnable.run();
        } catch (Throwable t) {
            errorCallback.error(t);
        }

        return this;
    }

    public void unsubsribe() {

    }
}
