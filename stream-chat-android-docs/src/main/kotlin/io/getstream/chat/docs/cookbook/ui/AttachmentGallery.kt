package io.getstream.chat.docs.cookbook.ui

import androidx.fragment.app.Fragment
import io.getstream.chat.android.ui.gallery.AttachmentGalleryDestination

/**
 * @see <a href="https://github.com/GetStream/stream-chat-android/wiki/UI-Cookbook#attachment-gallery">Attachment Gallery</a>
 */
class AttachmentGallery : Fragment() {

    fun navigatingToAttachmentGallery() {
        // Create Attachment Gallery Destination
        val destination = AttachmentGalleryDestination(
            requireContext(),
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
    }
}
