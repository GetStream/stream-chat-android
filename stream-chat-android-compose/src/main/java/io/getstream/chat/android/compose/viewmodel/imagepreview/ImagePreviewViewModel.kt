package io.getstream.chat.android.compose.viewmodel.imagepreview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.ChatDomain
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel responsible for loading and showing the images of a selected message.
 */
public class ImagePreviewViewModel(
    private val chatClient: ChatClient,
    private val chatDomain: ChatDomain,
    private val messageId: String,
) : ViewModel() {

    /**
     * The currently logged in user.
     */
    public val user: StateFlow<User?> = chatDomain.user

    /**
     * Represents the message that we observe to show the UI data.
     */
    public var message: Message by mutableStateOf(Message())
        private set

    /**
     * Shows or hides the image options menu and overlay in the UI.
     */
    public var isShowingOptions: Boolean by mutableStateOf(false)
        private set

    /**
     * Shows or hides the image gallery menu in the UI.
     */
    public var isShowingGallery: Boolean by mutableStateOf(false)
        private set

    /**
     * Loads the message data, which then updates the UI state to shows images.
     */
    init {
        chatClient.getMessage(messageId).enqueue { result ->
            if (result.isSuccess) {
                this.message = result.data()
            }
        }
    }

    /**
     * Toggles if we're showing the image options menu.
     *
     * @param isShowingOptions If we need to show or hide the options.
     */
    public fun toggleImageOptions(isShowingOptions: Boolean) {
        this.isShowingOptions = isShowingOptions
    }

    public fun toggleGallery(isShowingGallery: Boolean) {
        this.isShowingGallery = isShowingGallery
    }

    /**
     * Deletes the current image from the message we're observing. Updates
     */
    public fun deleteCurrentImage(currentImage: Attachment) {
        val imageUrl = currentImage.assetUrl ?: currentImage.imageUrl
        val message = message

        message.attachments.removeAll {
            it.assetUrl == imageUrl || it.imageUrl == imageUrl
        }

        chatDomain.editMessage(message).enqueue()
    }
}
