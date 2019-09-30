package com.getstream.sdk.chat.rest.controller;

import com.getstream.sdk.chat.interfaces.CachedTokenProvider;
import com.getstream.sdk.chat.rest.codecs.GsonConverter;
import com.getstream.sdk.chat.rest.core.ApiClientOptions;
import com.getstream.sdk.chat.rest.response.ErrorResponse;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit = null;
    private static String userToken = "";

    public static Retrofit getAuthorizedClient(CachedTokenProvider tokenProvider, ApiClientOptions options) {

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.level(HttpLoggingInterceptor.Level.BODY);

        TokenAuthInterceptor authInterceptor = new TokenAuthInterceptor(tokenProvider);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(options.getTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(options.getTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(options.getTimeout(), TimeUnit.MILLISECONDS)
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    Response response = chain.proceed(request);
                    if (!response.isSuccessful()) {
                        throw ErrorResponse.parseError(response);
                    }
                    return response;
                })
                .addInterceptor(loggingInterceptor)
                .addInterceptor(chain -> {
                    Request request = chain.request()
                            .newBuilder()
                            .addHeader("Content-Type", "application/json")
                            .addHeader("stream-auth-type", "jwt")
                            .addHeader("Accept-Encoding", "application/gzip")
                            .build();
                    return chain.proceed(request);
                })
                .addInterceptor(authInterceptor)
                .followRedirects(false)
                .build();

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(options.getHttpURL())
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(GsonConverter.Gson()))
                    .build();
        }
        return retrofit;
    }
}
