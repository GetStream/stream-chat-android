package com.getstream.sdk.chat.utils;

/*
 * Created by Anton Bevza on 2019-10-02.
 */
public interface ResultCallback<RESULT, ERROR> {
    void onSuccess(RESULT result);

    void onError(ERROR error);
}
