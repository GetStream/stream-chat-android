package io.getstream.chat.android.client.di

import android.content.Context
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.notifications.handler.NotificationHandler
import io.getstream.chat.android.client.token.TokenManager
import io.getstream.chat.android.client.uploader.FileUploader
import java.util.concurrent.Executor

/**
 * Release variant of [BaseChatModule].
 */
internal class ChatModule(
    appContext: Context,
    config: ChatClientConfig,
    notificationsHandler: NotificationHandler,
    uploader: FileUploader?,
    tokenManager: TokenManager,
    callbackExecutor: Executor?,
) : BaseChatModule(appContext, config, notificationsHandler, uploader, tokenManager, callbackExecutor)
