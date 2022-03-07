package io.getstream.chat.android.offline.message

import android.content.Context
import io.getstream.chat.android.offline.experimental.global.GlobalState
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.message.attachment.UploadAttachmentsWorker
import io.getstream.chat.android.offline.repository.RepositoryFacade
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.ConcurrentHashMap

/**
 * Factory to generate and provide instances of [MessageSendingService].
 */
internal object MessageSendingServiceFactory {
    private val messageSendingServices: ConcurrentHashMap<Pair<String, String>, MessageSendingService> =
        ConcurrentHashMap()

    /**
     * Create (if not available) and return an instance of [MessageSendingService].
     *
     * @param logic [LogicRegistry] that contains whole logic.
     * @param globalState [GlobalState] of the SDK.
     * @param channelType The type of the channel.
     * @param channelId The id of the channel.
     * @param scope [CoroutineScope] to launch asynchronous work.
     * @param repos [RepositoryFacade] to access and communicate with datasource.
     * @param context [Context] of the application.
     */
    fun getOrCreateService(
        logic: LogicRegistry,
        globalState: GlobalState,
        channelType: String,
        channelId: String,
        scope: CoroutineScope,
        repos: RepositoryFacade,
        context: Context,
    ): MessageSendingService =
        messageSendingServices.getOrPut(channelType to channelId) {
            MessageSendingService(
                logic,
                globalState,
                channelType,
                channelId,
                scope,
                repos,
                UploadAttachmentsWorker(context),
            )
        }

    fun getAllServices(): List<MessageSendingService> = messageSendingServices.values.toList()
}
