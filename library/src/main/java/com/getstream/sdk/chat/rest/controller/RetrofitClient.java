package com.getstream.sdk.chat.rest.controller;

import com.getstream.sdk.chat.interfaces.CachedTokenProvider;
import com.getstream.sdk.chat.rest.codecs.GsonConverter;
import com.getstream.sdk.chat.rest.core.ApiClientOptions;
import com.getstream.sdk.chat.rest.response.ErrorResponse;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();

    private static Request prepareRequest(Interceptor.Chain chain, boolean isAuthorizedClient) {
        String authType = isAuthorizedClient ? "jwt" : "anonymous";

        return chain.request()
                .newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("stream-auth-type", authType)
                .addHeader("Accept-Encoding", "application/gzip")
                .build();
    }

    public static Retrofit getClient(ApiClientOptions options, @Nullable CachedTokenProvider tokenProvider) {

        TokenAuthInterceptor authInterceptor = null;
        boolean isAuthorizedClient;

        if (tokenProvider != null) {
            isAuthorizedClient = true;
            authInterceptor = new TokenAuthInterceptor(tokenProvider);
        } else {
            isAuthorizedClient = false;
        }

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
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
                .addInterceptor(chain -> chain.proceed(prepareRequest(chain, isAuthorizedClient)))
                .followRedirects(false);

        if (authInterceptor != null) {
            clientBuilder.addInterceptor(authInterceptor);
        }

        return new Retrofit.Builder()
                .baseUrl(options.getHttpURL())
                .client(clientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create(GsonConverter.Gson()))
                .build();
    }

    public static Retrofit getAuthorizedCDNClient(CachedTokenProvider tokenProvider, ApiClientOptions options) {

        TokenAuthInterceptor authInterceptor = new TokenAuthInterceptor(tokenProvider);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(options.getCdntimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(options.getCdntimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(options.getCdntimeout(), TimeUnit.MILLISECONDS)
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    Response response = chain.proceed(request);
                    if (!response.isSuccessful()) {
                        throw ErrorResponse.parseError(response);
                    }
                    return response;
                })
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

        return new Retrofit.Builder()
                .baseUrl(options.getCdnHttpURL())
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(GsonConverter.Gson()))
                .build();
    }
}
