package io.getstream.chat.android.offline.experimental.plugin.listener

import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.experimental.plugin.listeners.SendMessageListener
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.experimental.global.GlobalState
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.message.MessageSendingServiceFactory
import io.getstream.chat.android.offline.repository.RepositoryFacade
import kotlinx.coroutines.CoroutineScope

@ExperimentalStreamChatApi
internal class SendMessageListenerImpl(
    private val context: Context,
    private val logic: LogicRegistry,
    private val globalState: GlobalState,
    private val scope: CoroutineScope,
    private val repos: RepositoryFacade,
) : SendMessageListener {

    override suspend fun onMessageSendResult(
        result: Result<Message>,
        channelType: String,
        channelId: String,
        message: Message,
    ) {
        val service = MessageSendingServiceFactory.getOrCreateService(
            logic,
            globalState,
            channelType,
            channelId,
            scope,
            repos,
            context,
            ChatClient.instance()
        )

        if (result.isSuccess) {
            service.handleSendMessageSuccess(result.data())
        } else {
            service.handleSendMessageFail(message, result.error())
        }
    }
}
