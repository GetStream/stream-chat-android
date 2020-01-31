package io.getstream.chat.android.core.poc.library.api

import com.facebook.stetho.okhttp3.StethoInterceptor
import io.getstream.chat.android.core.poc.library.CachedTokenProvider
import io.getstream.chat.android.core.poc.library.ChatClientBuilder
import io.getstream.chat.android.core.poc.library.gson.JsonParserImpl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit


object RetrofitClient {

    private val TAG = RetrofitClient::class.java.simpleName

    fun buildClient(
        options: ApiClientOptions,
        jsonParser: JsonParserImpl,
        config: ChatClientBuilder.ChatConfig
    ): Retrofit {
        return buildClient(
            options.httpURL,
            options.timeout.toLong(),
            options.timeout.toLong(),
            options.timeout.toLong(),
            config,
            jsonParser
        )
    }

    fun getAuthorizedCDNClient(
        config: ChatClientBuilder.ChatConfig,
        options: ApiClientOptions,
        jsonParser: JsonParserImpl
    ): Retrofit {
        return buildClient(
            options.cdnHttpURL,
            options.cdntimeout.toLong(),
            options.cdntimeout.toLong(),
            options.cdntimeout.toLong(),
            config,
            jsonParser
        )
    }

    private fun buildClient(
        endpoint: String,
        connectTimeout: Long,
        writeTimeout: Long,
        readTimeout: Long,
        config:ChatClientBuilder.ChatConfig,
        jsonParser: JsonParserImpl
    ): Retrofit {

        val clientBuilder = OkHttpClient.Builder()
            .followRedirects(false)
            // timeouts
            .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
            .writeTimeout(writeTimeout, TimeUnit.MILLISECONDS)
            .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
            // interceptors
            .addInterceptor(HeadersInterceptor(config))
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor(TokenAuthInterceptor(config, jsonParser))
            .addNetworkInterceptor(StethoInterceptor())

        val builder = Retrofit.Builder()
            .baseUrl(endpoint)
            .client(clientBuilder.build())

        return jsonParser.configRetrofit(builder).build()
    }


}
