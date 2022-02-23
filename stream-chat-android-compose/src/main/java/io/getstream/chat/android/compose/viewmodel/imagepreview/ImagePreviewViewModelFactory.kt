package io.getstream.chat.android.compose.viewmodel.imagepreview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.experimental.global.GlobalState

/**
 * Holds the dependencies required for the Image Preview Screen.
 * Currently builds the [ImagePreviewViewModel] using those dependencies.
 */
public class ImagePreviewViewModelFactory(
    private val chatClient: ChatClient,
    private val globalState: GlobalState,
    private val messageId: String,
) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of [ImagePreviewViewModel] class.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ImagePreviewViewModel(chatClient, globalState, messageId) as T
    }
}
