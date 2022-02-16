package io.getstream.chat.android.offline.message.experimental

import android.content.Context
import io.getstream.chat.android.offline.experimental.global.GlobalState
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry
import io.getstream.chat.android.offline.message.attachment.UploadAttachmentsWorker
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.ConcurrentHashMap

internal class MessageSendingServiceFactory(
    private val context: Context,
    private val logicRegistry: LogicRegistry,
    private val stateRegistry: StateRegistry,
    private val globalState: GlobalState,
    private val scope: CoroutineScope,
) {

    private val messagePrepHandlers: ConcurrentHashMap<Pair<String, String>, MessageSendingService> =
        ConcurrentHashMap()

    fun getOrCreate(channelType: String, channelId: String): MessageSendingService {
        return messagePrepHandlers.getOrPut(channelType to channelId) {
            MessageSendingService(
                logicRegistry,
                stateRegistry.channel(channelType, channelId),
                globalState,
                UploadAttachmentsWorker(context),
                scope
            )
        }
    }
}
