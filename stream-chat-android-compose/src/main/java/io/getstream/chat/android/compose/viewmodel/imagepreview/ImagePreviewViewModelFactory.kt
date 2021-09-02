package io.getstream.chat.android.compose.viewmodel.imagepreview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.getstream.chat.android.client.ChatClient

/**
 * Holds the dependencies required for the Image Preview Screen.
 * Currently builds the [ImagePreviewViewModel] using those dependencies.
 */
public class ImagePreviewViewModelFactory(
    private val chatClient: ChatClient,
    private val messageId: String,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ImagePreviewViewModel(chatClient, messageId) as T
    }
}
