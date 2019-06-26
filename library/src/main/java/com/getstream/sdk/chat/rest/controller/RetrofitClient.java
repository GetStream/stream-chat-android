package com.getstream.sdk.chat.rest.controller;

import android.util.Log;

import com.getstream.sdk.chat.rest.BaseURL;
import com.getstream.sdk.chat.utils.Global;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String TAG = RetrofitClient.class.getSimpleName();

    public static final String BASE_URL = Global.baseURL.url(BaseURL.Scheme.https);
    private static Retrofit retrofit = null;

    public static Retrofit getAuthrizedClient() {

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .addInterceptor(loggingInterceptor)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request()
                                .newBuilder()
                                .addHeader("Authorization", Global.streamChat.getUserToken())
                                .addHeader("Content-Type", "application/json")
                                .addHeader("stream-auth-type", "jwt")
                                .addHeader("Accept-Encoding", "application/gzip")
                                .build();
                        Response response = chain.proceed(request);
                        Log.d(TAG, "Return Correct channelResponse");
                        return response;
                    }
                })
                .build();

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        Log.d(TAG, "Return bad channelResponse");
        return retrofit;
    }
}
