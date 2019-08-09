package com.getstream.sdk.chat.rest.controller;

import com.getstream.sdk.chat.rest.codecs.GsonConverter;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit = null;

    public static Retrofit getAuthorizedClient(String baseURL, String userToken) {

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    Response response = chain.proceed(request);
                    if (!response.isSuccessful()) {
                        String message = response.body().string();
                        throw new IOException(message);
                    }
                    return response;
                })
                .addInterceptor(loggingInterceptor)
                .addInterceptor(chain -> {
                    Request request = chain.request()
                            .newBuilder()
                            .addHeader("Authorization", userToken)
                            .addHeader("Content-Type", "application/json")
                            .addHeader("stream-auth-type", "jwt")
                            .addHeader("Accept-Encoding", "application/gzip")
                            .build();
                    Response response = chain.proceed(request);
                    return response;
                })
                .build();

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseURL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(GsonConverter.Gson()))
                    .build();
        }
        return retrofit;
    }
}
