package io.getstream.chat.android.compose.viewmodel.messages

import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.getstream.sdk.chat.utils.StorageHelper
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.handlers.ClipboardHandlerImpl
import io.getstream.chat.android.compose.ui.util.StorageHelperWrapper
import io.getstream.chat.android.offline.ChatDomain

/**
 * Holds all the dependencies needed to build the ViewModels for the Messages Screen.
 * Currently builds the [MessageComposerViewModel], [MessageListViewModel] and [AttachmentsPickerViewModel].
 * */
public class MessagesViewModelFactory(
    private val context: Context,
    private val clipboardManager: ClipboardManager,
    private val chatClient: ChatClient,
    private val chatDomain: ChatDomain,
    private val channelId: String,
    private val enforceUniqueReactions: Boolean,
    private val messageLimit: Int,
) : ViewModelProvider.Factory {

    private val factories: Map<Class<*>, () -> ViewModel> = mapOf(
        MessageComposerViewModel::class.java to {
            MessageComposerViewModel(chatClient, chatDomain, channelId)
        },
        MessageListViewModel::class.java to {
            MessageListViewModel(
                chatClient,
                chatDomain,
                channelId,
                messageLimit,
                enforceUniqueReactions,
                ClipboardHandlerImpl(clipboardManager)
            )
        },
        AttachmentsPickerViewModel::class.java to {
            AttachmentsPickerViewModel(
                StorageHelperWrapper(context, StorageHelper())
            )
        }
    )

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel: ViewModel = factories[modelClass]?.invoke()
            ?: throw IllegalArgumentException("MessageListViewModelFactory can only create instances of the following classes: ${factories.keys.joinToString { it.simpleName }}")

        @Suppress("UNCHECKED_CAST")
        return viewModel as T
    }
}
