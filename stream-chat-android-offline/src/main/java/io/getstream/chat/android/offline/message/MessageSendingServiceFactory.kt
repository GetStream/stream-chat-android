package io.getstream.chat.android.offline.message

import android.content.Context
import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.experimental.global.GlobalState
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.message.attachment.UploadAttachmentsWorker
import io.getstream.chat.android.offline.repository.RepositoryFacade
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.ConcurrentHashMap

/**
 * Factory to generate and provide instances of [MessageSendingService].
 */
@ExperimentalStreamChatApi
internal class MessageSendingServiceFactory private constructor() {
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

    internal companion object {
        private var instance: MessageSendingServiceFactory? = null

        /**
         * Gets the singleton of [MessageSendingServiceFactory] or creates it in the first call.
         */
        internal fun getOrCreate(): MessageSendingServiceFactory {
            return instance ?: MessageSendingServiceFactory().also { factory ->
                instance = factory
            }
        }

        /**
         * Creates an instance of [MessageSendingServiceFactory] with a fresh state. Please keep in mind that many instances of this class may
         * cause the SDK to present an inconsistent state.
         */
        @VisibleForTesting
        internal fun create(): MessageSendingServiceFactory = MessageSendingServiceFactory()
    }
}
