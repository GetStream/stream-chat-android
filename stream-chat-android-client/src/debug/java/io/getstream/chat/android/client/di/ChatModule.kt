package io.getstream.chat.android.client.di

import android.content.Context
import com.facebook.stetho.Stetho
import com.facebook.stetho.okhttp3.StethoInterceptor
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.token.TokenManager
import io.getstream.chat.android.client.uploader.FileUploader
import okhttp3.OkHttpClient

// TODO: what's the point of this module system, its complexity with no clear benefit
internal class ChatModule(appContext: Context, config: ChatClientConfig, notificationsHandler: ChatNotificationHandler, uploader: FileUploader? = null, tokenManager: TokenManager) : BaseChatModule(appContext, config, notificationsHandler, uploader, tokenManager) {

    init {
        Stetho.initializeWithDefaults(appContext)
    }

    override fun clientBuilder(
        connectTimeout: Long,
        writeTimeout: Long,
        readTimeout: Long,
        config: ChatClientConfig,
        parser: ChatParser
    ): OkHttpClient.Builder {
        return super.clientBuilder(connectTimeout, writeTimeout, readTimeout, config, parser)
            .addNetworkInterceptor(StethoInterceptor())
    }
}
