package com.getstream.sdk.chat.utils;

/**
 * Early replacement of RxJava to map current callbacks system
 */
public interface Subscription<T> {
    Observable<T> subscribe(SuccessCallback<T> successCallback, ErrorCallback errorCallback);
}