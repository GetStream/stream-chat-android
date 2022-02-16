package io.getstream.chat.android.offline.message

import android.content.Context
import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.experimental.global.GlobalMutableState
import io.getstream.chat.android.offline.experimental.global.GlobalState
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.message.attachment.UploadAttachmentsWorker
import io.getstream.chat.android.offline.repository.RepositoryFacade
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.ConcurrentHashMap

@ExperimentalStreamChatApi
internal class MessageSendingServiceFactory {
    private val messageSendingServices: ConcurrentHashMap<Pair<String, String>, MessageSendingService> =
        ConcurrentHashMap()

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
         * Gets the singleton of [GlobalMutableState] or creates it in the first call.
         */
        internal fun getOrCreate(): MessageSendingServiceFactory {
            return instance ?: MessageSendingServiceFactory().also { factory ->
                instance = factory
            }
        }

        @VisibleForTesting
        internal fun create(): MessageSendingServiceFactory = MessageSendingServiceFactory()
    }
}
