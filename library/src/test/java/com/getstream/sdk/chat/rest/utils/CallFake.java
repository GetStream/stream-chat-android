package com.getstream.sdk.chat.rest.utils;

import android.accounts.NetworkErrorException;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
 * Created by Anton Bevza on 2019-10-25.
 */
public class CallFake<T> implements Call<T> {

    private Response<T> response;

    public CallFake(Response<T> response) {
        this.response = response;
    }

    public static <T> CallFake<T> buildSuccess(T body) {
        return new CallFake<>(Response.success(body));
    }

    @Override
    public Response<T> execute() {
        return response;
    }

    @Override
    public void enqueue(Callback<T> callback) {
        if (response.isSuccessful()) {
            callback.onResponse(this, response);
        } else {
            callback.onFailure(this, new NetworkErrorException());
        }
    }

    @Override
    public boolean isExecuted() {
        return false;
    }

    @Override
    public void cancel() {

    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public Call<T> clone() {
        return null;
    }

    @Override
    public Request request() {
        return null;
    }
}
