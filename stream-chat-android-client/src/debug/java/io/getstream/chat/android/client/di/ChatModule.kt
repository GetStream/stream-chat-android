package io.getstream.chat.android.client.di

import android.content.Context
import com.facebook.stetho.Stetho
import com.facebook.stetho.okhttp3.StethoInterceptor
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.parser.ChatParser
import okhttp3.OkHttpClient

internal class ChatModule(
    appContext: Context,
    config: ChatClientConfig
) : BaseChatModule(appContext, config) {

    init {
        Stetho.initializeWithDefaults(appContext)
    }

    override fun clientBuilder(
        connectTimeout: Long,
        writeTimeout: Long,
        readTimeout: Long,
        config: ChatClientConfig,
        parser: ChatParser,
        isAnonymousApi: Boolean
    ): OkHttpClient.Builder {
        return super.clientBuilder(
            connectTimeout,
            writeTimeout,
            readTimeout,
            config,
            parser,
            isAnonymousApi
        ).addNetworkInterceptor(StethoInterceptor())
    }
}
