package io.getstream.chat.android.compose.viewmodel.imagepreview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Message

/**
 * ViewModel responsible for loading and showing the images of a selected message.
 * */
public class ImagePreviewViewModel(
    private val chatClient: ChatClient
) : ViewModel() {

    public var message: Message by mutableStateOf(Message())
        private set

    /**
     * Loads the message data, which then updates the UI state and shows images.
     * */
    public fun start(messageId: String) {
        chatClient.getMessage(messageId).enqueue { result ->
            if (result.isSuccess) {
                this.message = result.data()
            }
        }
    }
}
