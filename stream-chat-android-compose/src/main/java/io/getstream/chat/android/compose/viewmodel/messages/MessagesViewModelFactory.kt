package io.getstream.chat.android.compose.viewmodel.messages

import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.getstream.sdk.chat.utils.AttachmentConstants
import com.getstream.sdk.chat.utils.StorageHelper
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.common.composer.MessageComposerController
import io.getstream.chat.android.compose.handlers.ClipboardHandlerImpl
import io.getstream.chat.android.compose.ui.util.StorageHelperWrapper
import io.getstream.chat.android.offline.ChatDomain

/**
 * Holds all the dependencies needed to build the ViewModels for the Messages Screen.
 * Currently builds the [MessageComposerViewModel], [MessageListViewModel] and [AttachmentsPickerViewModel].
 * @param context Used to build the [ClipboardManager].
 * @param channelId The current channel ID, to load the messages from.
 * @param chatClient The client to use for API calls.
 * @param chatDomain The domain used to fetch data.
 * @param enforceUniqueReactions Flag to enforce unique reactions or enable multiple from the same user.
 * @param messageLimit The limit when loading messages.
 * @param maxAttachmentCount The maximum number of attachments that can be sent in a single message.
 * @param maxAttachmentSize Tne maximum file size of each attachment in bytes. By default, 20mb for Stream CDN.
 * @param showDateSeparators If we should show date separator items in the list.
 * @param showSystemMessages If we should show system message items in the list.
 */
public class MessagesViewModelFactory(
    private val context: Context,
    private val channelId: String,
    private val chatClient: ChatClient = ChatClient.instance(),
    private val chatDomain: ChatDomain = ChatDomain.instance(),
    private val enforceUniqueReactions: Boolean = true,
    private val messageLimit: Int = 30,
    private val maxAttachmentCount: Int = AttachmentConstants.MAX_ATTACHMENTS_COUNT,
    private val maxAttachmentSize: Long = AttachmentConstants.MAX_UPLOAD_FILE_SIZE,
    private val showDateSeparators: Boolean = true,
    private val showSystemMessages: Boolean = true,
) : ViewModelProvider.Factory {

    /**
     * The list of factories that can build [ViewModel]s that our Messages feature components use.
     */
    private val factories: Map<Class<*>, () -> ViewModel> = mapOf(
        MessageComposerViewModel::class.java to {
            MessageComposerViewModel(
                MessageComposerController(
                    chatClient = chatClient,
                    chatDomain = chatDomain,
                    channelId = channelId,
                    maxAttachmentCount = maxAttachmentCount,
                    maxAttachmentSize = maxAttachmentSize
                )
            )
        },
        MessageListViewModel::class.java to {
            MessageListViewModel(
                chatClient = chatClient,
                chatDomain = chatDomain,
                channelId = channelId,
                messageLimit = messageLimit,
                enforceUniqueReactions = enforceUniqueReactions,
                clipboardHandler = ClipboardHandlerImpl(context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager),
                showDateSeparators = showDateSeparators,
                showSystemMessages = showSystemMessages
            )
        },
        AttachmentsPickerViewModel::class.java to {
            AttachmentsPickerViewModel(
                StorageHelperWrapper(context, StorageHelper())
            )
        }
    )

    /**
     * Creates the required [ViewModel] for our use case, based on the [factories] we provided.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel: ViewModel = factories[modelClass]?.invoke()
            ?: throw IllegalArgumentException("MessageListViewModelFactory can only create instances of the following classes: ${factories.keys.joinToString { it.simpleName }}")

        @Suppress("UNCHECKED_CAST")
        return viewModel as T
    }
}
