package io.getstream.chat.android.core.poc.library.api

import com.google.gson.GsonBuilder
import io.getstream.chat.android.core.poc.library.CachedTokenProvider
import io.getstream.chat.android.core.poc.library.gson.JsonParserImpl
import io.getstream.chat.android.core.poc.library.json.ChatGson
import io.getstream.chat.android.core.poc.library.json.ConverterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit


object RetrofitClient {

    private val TAG = RetrofitClient::class.java.simpleName

    fun buildClient(
        options: ApiClientOptions,
        tokenProvider: () -> CachedTokenProvider?,
        anonymousAuth: () -> Boolean,
        jsonParser: JsonParserImpl
    ): Retrofit {
        return buildClient(
            options.httpURL,
            options.timeout.toLong(),
            options.timeout.toLong(),
            options.timeout.toLong(),
            tokenProvider,
            anonymousAuth,
            jsonParser
        )
    }

    fun getAuthorizedCDNClient(
        tokenProvider: () -> CachedTokenProvider,
        options: ApiClientOptions,
        jsonParser: JsonParserImpl
    ): Retrofit {
        return buildClient(
            options.cdnHttpURL,
            options.cdntimeout.toLong(),
            options.cdntimeout.toLong(),
            options.cdntimeout.toLong(),
            tokenProvider,
            { false },
            jsonParser
        )
    }


    private fun buildClient(
        endpoint: String,
        connectTimeout: Long,
        writeTimeout: Long,
        readTimeout: Long,
        tokenProvider: () -> CachedTokenProvider?,
        anonymousAuth: () -> Boolean
        jsonParser: JsonParserImpl
    ): Retrofit {

        val clientBuilder = OkHttpClient.Builder()
            .followRedirects(false)
            // timeouts
            .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
            .writeTimeout(writeTimeout, TimeUnit.MILLISECONDS)
            .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
            // interceptors
            .addInterceptor(HeadersInterceptor(anonymousAuth))
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor(TokenAuthInterceptor(tokenProvider, anonymousAuth, jsonParser))

        val builder = Retrofit.Builder()
            .baseUrl(endpoint)
            .client(clientBuilder.build())

        return jsonParser.configRetrofit(builder).build()
    }

    private fun prepareRequest(
        chain: Interceptor.Chain,
        isAnonymousClient: Boolean?
    ): Request {
        val authType = if (isAnonymousClient == true) "anonymous" else "jwt"
        return chain.request()
            .newBuilder()
            .addHeader("Content-Type", "application/json")
            .addHeader("stream-auth-type", authType)
            .addHeader("Accept-Encoding", "application/gzip")
            .build()
    }

    private fun createGsonConverter(
        type: Type,
        typeAdapter: Any
    ): Converter.Factory {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(type, typeAdapter)
        return GsonConverterFactory.create(gsonBuilder.create())
    }


}
