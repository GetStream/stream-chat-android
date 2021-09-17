package io.getstream.chat.android.client.di

import android.content.Context
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.token.TokenManager
import io.getstream.chat.android.client.uploader.FileUploader
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.Executor

/**
 * Debug implementation of [BaseChatModule].
 *
 * When updating this class, don't forget to update its empty release variant as well, as their
 * interfaces have to match.
 */
internal class ChatModule(
    appContext: Context,
    config: ChatClientConfig,
    notificationsHandler: ChatNotificationHandler,
    uploader: FileUploader?,
    tokenManager: TokenManager,
    callbackExecutor: Executor?,
    loggingInterceptor: Interceptor,
) : BaseChatModule(
    appContext,
    config,
    notificationsHandler,
    uploader,
    tokenManager,
    callbackExecutor,
    loggingInterceptor
) {

    override fun clientBuilder(
        timeout: Long,
        config: ChatClientConfig,
        parser: ChatParser,
        isAnonymousApi: Boolean,
    ): OkHttpClient.Builder {
        return super.clientBuilder(
            timeout,
            config,
            parser,
            isAnonymousApi
        ).addNetworkInterceptor(flipperInterceptor())
    }
}
