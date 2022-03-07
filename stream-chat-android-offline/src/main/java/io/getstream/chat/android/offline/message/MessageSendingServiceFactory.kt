package io.getstream.chat.android.offline.message

import android.content.Context
import io.getstream.chat.android.offline.experimental.global.GlobalState
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry
import io.getstream.chat.android.offline.message.attachment.UploadAttachmentsNetworkType
import io.getstream.chat.android.offline.repository.RepositoryFacade
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.ConcurrentHashMap

/**
 * Factory to generate and provide instances of [MessageSendingService].
 */
internal class MessageSendingServiceFactory private constructor(
    private val logic: LogicRegistry,
    private val globalState: GlobalState,
    private val scope: CoroutineScope,
    private val repos: RepositoryFacade,
    private val networkType: UploadAttachmentsNetworkType,
) {
    private val messageSendingServices: ConcurrentHashMap<Pair<String, String>, MessageSendingService> =
        ConcurrentHashMap()

    /**
     * Create (if not available) and return an instance of [MessageSendingService].
     *
     * @param channelType The type of the channel.
     * @param channelId The id of the channel.
     * @param context [Context] of the application.
     */
    fun getOrCreateService(
        context: Context,
        channelType: String,
        channelId: String,
    ): MessageSendingService =
        messageSendingServices.getOrPut(channelType to channelId) {
            MessageSendingService(
                logic,
                globalState,
                channelType,
                channelId,
                scope,
                repos,
                context,
                networkType
            )
        }

    fun getAllServices(): List<MessageSendingService> = messageSendingServices.values.toList()

    internal companion object {
        private var instance: MessageSendingServiceFactory? = null

        /**
         * Gets the singleton of LogicRegistry or creates it in the first call
         *
         * @param stateRegistry [StateRegistry]
         */
        internal fun getOrCreate(
            logic: LogicRegistry,
            globalState: GlobalState,
            scope: CoroutineScope,
            repos: RepositoryFacade,
            networkType: UploadAttachmentsNetworkType,
        ): MessageSendingServiceFactory {
            return instance ?: MessageSendingServiceFactory(
                logic,
                globalState,
                scope,
                repos,
                networkType
            ).also { factory ->
                instance = factory
            }
        }
    }
}
