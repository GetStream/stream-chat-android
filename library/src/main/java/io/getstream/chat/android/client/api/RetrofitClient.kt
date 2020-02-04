package io.getstream.chat.android.client.api

import com.facebook.stetho.okhttp3.StethoInterceptor
import io.getstream.chat.android.client.gson.JsonParser
import io.getstream.chat.android.client.gson.JsonParserImpl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit


object RetrofitClient {

    private val TAG = RetrofitClient::class.java.simpleName

    fun buildClient(
        options: ChatConfig,
        jsonParser: JsonParser,
        config: ChatConfig
    ): Retrofit {
        return buildClient(
            options.httpURL,
            options.baseTimeout.toLong(),
            options.baseTimeout.toLong(),
            options.baseTimeout.toLong(),
            config,
            jsonParser
        )
    }

    fun getAuthorizedCDNClient(
        config: ChatConfig,
        options: ChatConfig,
        jsonParser: JsonParserImpl
    ): Retrofit {
        return buildClient(
            options.cdnHttpURL,
            options.cdnTimeout.toLong(),
            options.cdnTimeout.toLong(),
            options.cdnTimeout.toLong(),
            config,
            jsonParser
        )
    }

    private fun buildClient(
        endpoint: String,
        connectTimeout: Long,
        writeTimeout: Long,
        readTimeout: Long,
        config: ChatConfig,
        jsonParser: JsonParser
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
