package io.getstream.chat.android.ui.message.list.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.common.composer.MessageComposerController
import io.getstream.chat.android.ui.message.composer.MessageComposerViewModel
import io.getstream.chat.android.ui.message.list.header.viewmodel.MessageListHeaderViewModel

/**
 * A ViewModel factory for MessageListViewModel, MessageListHeaderViewModel and MessageInputViewModel.
 *
 * @param cid The channel id in the format messaging:123.
 * @param messageId The id of the target message to displayed.
 *
 * @see MessageListViewModel
 * @see MessageListHeaderViewModel
 * @see MessageInputViewModel
 */
public class MessageListViewModelFactory @JvmOverloads constructor(
    private val cid: String,
    private val messageId: String? = null,
) : ViewModelProvider.Factory {

    private val factories: Map<Class<*>, () -> ViewModel> = mapOf(
        MessageListHeaderViewModel::class.java to { MessageListHeaderViewModel(cid) },
        MessageInputViewModel::class.java to { MessageInputViewModel(cid) },
        MessageListViewModel::class.java to { MessageListViewModel(cid, messageId) },
        MessageComposerViewModel::class.java to { MessageComposerViewModel(MessageComposerController(cid)) },
    )

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel: ViewModel = factories[modelClass]?.invoke()
            ?: throw IllegalArgumentException("MessageListViewModelFactory can only create instances of the following classes: ${factories.keys.joinToString { it.simpleName }}")

        @Suppress("UNCHECKED_CAST")
        return viewModel as T
    }
}
