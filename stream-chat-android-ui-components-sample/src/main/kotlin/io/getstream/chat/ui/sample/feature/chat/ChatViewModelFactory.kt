package io.getstream.chat.ui.sample.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.getstream.chat.ui.sample.feature.chat.info.ChatInfoViewModel
import io.getstream.chat.ui.sample.feature.chat.info.group.GroupChatInfoViewModel

class ChatViewModelFactory(private val cid: String) : ViewModelProvider.Factory {
    private val factories: Map<Class<*>, () -> ViewModel> = mapOf(
        ChatViewModel::class.java to { ChatViewModel(cid) },
        ChatInfoViewModel::class.java to { ChatInfoViewModel(cid) },
        GroupChatInfoViewModel::class.java to { GroupChatInfoViewModel(cid) },
    )

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel: ViewModel = factories[modelClass]?.invoke()
            ?: throw IllegalArgumentException("ChatViewModelFactory can only create instances of the following classes: ${factories.keys.joinToString { it.simpleName }}")

        @Suppress("UNCHECKED_CAST")
        return viewModel as T
    }
}
