// ktlint-disable filename

package io.getstream.chat.docs.kotlin.ui.utility

import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.feature.gallery.AttachmentGalleryDestination
import io.getstream.chat.android.ui.feature.messages.list.MessageListView

/**
 * [Attachment Gallery](https://getstream.io/chat/docs/sdk/android/ui/utility-components/attachment-gallery/)
 */
private class AttachmentGallery {

    private lateinit var messageListView: MessageListView

    /**
     * [Handling Actions](https://getstream.io/chat/docs/sdk/android/ui/utility-components/attachment-gallery/#handling-actions)
     */
    fun handlingActions() {
        messageListView.setAttachmentReplyOptionClickHandler { resultItem ->
            resultItem.messageId
            // Handle reply to attachment
        }

        messageListView.setAttachmentShowInChatOptionClickHandler { resultItem ->
            resultItem.messageId
            // Handle show in chat
        }

        messageListView.setDownloadOptionHandler { resultItem ->
            resultItem.assetUrl
            // Handle download the attachment
        }

        messageListView.setAttachmentDeleteOptionClickHandler { resultItem ->
            resultItem.assetUrl
            resultItem.imageUrl
            // Handle delete
        }
    }

    /**
     * [Navigating to Attachment Gallery](https://getstream.io/chat/docs/sdk/android/ui/utility-components/attachment-gallery/#navigating-to-attachment-gallery)
     */
    fun navigatingToAttachmentGallery(activity: AppCompatActivity) {
        // Create Attachment Gallery Destination
        val destination = AttachmentGalleryDestination(
            activity,
            attachmentReplyOptionHandler = { resultItem ->
                // Handle reply
            },
            attachmentShowInChatOptionHandler = { resultItem ->
                // Handle show image in chat
            },
            attachmentDownloadOptionHandler = { resultItem ->
                // Handle download image
            },
            attachmentDeleteOptionClickHandler = { resultItem ->
                // Handle delete image
            },
        )

        // Register destination with the ActivityResultRegistry
        activity?.activityResultRegistry?.let { registry ->
            destination.register(registry)
        }

        // Set the data to display
        destination.setData(attachmentGalleryItems = listOf(), attachmentIndex = 0)

        // Fire the navigation request
        ChatUI.navigator.navigate(destination)
    }
}
