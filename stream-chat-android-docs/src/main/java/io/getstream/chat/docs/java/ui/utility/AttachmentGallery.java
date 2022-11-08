package io.getstream.chat.docs.java.ui.utility;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Collections;

import io.getstream.chat.android.ui.ChatUI;
import io.getstream.chat.android.ui.feature.gallery.AttachmentGalleryDestination;
import io.getstream.chat.android.ui.feature.messages.list.MessageListView;

/**
 * [Attachment Gallery](https://getstream.io/chat/docs/sdk/android/ui/utility-components/attachment-gallery/)
 */
public class AttachmentGallery {

    private MessageListView messageListView;

    public void handlingActions() {
        messageListView.setAttachmentReplyOptionClickHandler(resultItem -> {
            resultItem.getMessageId();
            // Handle reply to attachment
        });

        messageListView.setAttachmentShowInChatOptionClickHandler(resultItem -> {
            resultItem.getMessageId();
            // Handle show in chat
        });

        messageListView.setDownloadOptionHandler(resultItem -> {
            resultItem.getAssetUrl();
            // Handle download the attachment
        });

        messageListView.setAttachmentDeleteOptionClickHandler(resultItem -> {
            resultItem.getAssetUrl();
            resultItem.getImageUrl();
            // Handle delete
        });
    }

    public void navigatingToAttachmentGallery(AppCompatActivity activity) {
        // Create Attachment Gallery Destination
        AttachmentGalleryDestination destination = new AttachmentGalleryDestination(
                activity,
                resultItem -> {
                    // Handle reply
                },
                resultItem -> {
                    // Handle show image in chat
                },
                resultItem -> {
                    // Handle download image
                },
                resultItem -> {
                    // Handle delete image
                }
        );

        // Register destination with the ActivityResultRegistry
        destination.register(activity.getActivityResultRegistry());

        // Set the data to display
        int attachmentIndex = 0;
        destination.setData(Collections.emptyList(), attachmentIndex);

        // Fire the navigation request
        ChatUI.getNavigator().navigate(destination);
    }
}
