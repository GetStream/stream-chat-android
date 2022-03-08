package io.getstream.chat.android.ui.typing.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.ui.typing.viewmodel.TypingIndicatorViewModel

public class TypingIndicatorViewModelFactory(
    private val cid: String,
    private val chatClient: ChatClient = ChatClient.instance(),
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == TypingIndicatorViewModel::class.java) {
            "TypingIndicatorViewModelFactory can only create instances of TypingIndicatorViewModel"
        }

        @Suppress("UNCHECKED_CAST")
        return TypingIndicatorViewModel(cid, chatClient) as T
    }
}
