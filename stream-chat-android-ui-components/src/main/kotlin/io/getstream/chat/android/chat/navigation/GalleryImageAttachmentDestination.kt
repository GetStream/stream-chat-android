package io.getstream.chat.android.chat.navigation

import android.content.Context
import com.getstream.sdk.chat.navigation.destinations.AttachmentDestination
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.images.AttachmentGalleryActivity

public class GalleryImageAttachmentDestination(
    message: Message,
    attachment: Attachment,
    context: Context
) : AttachmentDestination(message, attachment, context) {
    override fun showImagesWithCurrentIndex(currentIndex: Int, attachmentUrls: List<String>) {
        start(AttachmentGalleryActivity.createIntent(context, currentIndex, attachmentUrls))
    }
}