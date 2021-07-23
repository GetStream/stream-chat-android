package io.getstream.chat.android.compose.viewmodel.imagepreview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.offline.ChatDomain

class ImagePreviewViewModelFactory(
    private val chatClient: ChatClient,
    private val chatDomain: ChatDomain,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ImagePreviewViewModel(chatClient, chatDomain) as T
    }
}
