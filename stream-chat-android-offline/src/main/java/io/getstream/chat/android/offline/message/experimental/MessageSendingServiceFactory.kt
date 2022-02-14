package io.getstream.chat.android.offline.message.experimental

import android.content.Context
import io.getstream.chat.android.offline.experimental.channel.state.ChannelState
import io.getstream.chat.android.offline.experimental.global.GlobalState
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.message.attachment.UploadAttachmentsWorker
import kotlinx.coroutines.CoroutineScope

internal class MessageSendingServiceFactory {
    fun create(
        context: Context,
        logicRegistry: LogicRegistry,
        channelState: ChannelState,
        globalState: GlobalState,
        scope: CoroutineScope,
    ): MessageSendingService =
        MessageSendingService(logicRegistry, channelState, globalState, UploadAttachmentsWorker(context), scope)
}
