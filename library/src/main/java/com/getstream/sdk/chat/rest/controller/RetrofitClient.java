package com.getstream.sdk.chat.rest.controller;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.interfaces.CachedTokenProvider;
import com.getstream.sdk.chat.rest.codecs.GsonConverter;
import com.getstream.sdk.chat.rest.core.ApiClientOptions;
import com.getstream.sdk.chat.rest.response.ErrorResponse;

import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String TAG = RetrofitClient.class.getSimpleName();
    private static HttpLoggingInterceptor loggingInterceptor;

    private static Request prepareRequest(Interceptor.Chain chain, boolean isAnonymousClient) {
        String authType = isAnonymousClient ? "anonymous" : "jwt";

        return chain.request()
                .newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("stream-auth-type", authType)
                .addHeader("Accept-Encoding", "application/gzip")
                .build();
    }

    public static Retrofit getClient(ApiClientOptions options, CachedTokenProvider tokenProvider, boolean anonymousAuth) {

        if (tokenProvider != null && anonymousAuth) {
            StreamChat.getLogger().logE(RetrofitClient.class, "Can\'t use anonymous mode with tokenProvider. TokenProvider will be ignored");
        }

        if (tokenProvider == null && !anonymousAuth) {
            StreamChat.getLogger().logE(RetrofitClient.class, "tokenProvider must be non-null in not anonymous mode");
            return null;
        }

        TokenAuthInterceptor authInterceptor = null;

        if (!anonymousAuth) {
            authInterceptor = new TokenAuthInterceptor(tokenProvider);
        }

        setupLoggingInterceptor();

        ConnectionPool cp = new ConnectionPool(10, options.getKeepAliveDuration(), TimeUnit.MILLISECONDS);

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(options.getTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(options.getTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(options.getTimeout(), TimeUnit.MILLISECONDS)
                .connectionPool(cp)
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    Response response = chain.proceed(request);
                    if (!response.isSuccessful()) {
                        throw ErrorResponse.parseError(response);
                    }
                    return response;
                })
                .addInterceptor(loggingInterceptor)
                .addInterceptor(chain -> chain.proceed(prepareRequest(chain, anonymousAuth)))
                .followRedirects(false);

        if (!anonymousAuth) {
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

    private static HttpLoggingInterceptor setupLoggingInterceptor() {
        loggingInterceptor = new HttpLoggingInterceptor();

        // Uncomment if need show http logs
        loggingInterceptor.level(HttpLoggingInterceptor.Level.BODY);
        return loggingInterceptor;
    }
}
