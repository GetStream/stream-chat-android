package io.getstream.chat.ui.sample

import android.content.Context
import android.content.Intent
import com.getstream.sdk.chat.navigation.destinations.ChatDestination
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message

public class MultimediaAttachmentDestination(
    private val message: Message,
    private val attachment: Attachment,
    context: Context,
) : ChatDestination(context) {

    override fun navigate() {
        if (containsPlayableContent(attachment)) {
            val intent = Intent(context, DummyActivity::class.java)
            start(intent)
        }
    }

    public companion object {

        public fun containsPlayableContent(attachment: Attachment): Boolean =
            attachment.mimeType?.contains("audio") == true
                || attachment.mimeType?.contains("video") == true
                || attachment.type == "audio"
                || attachment.type == "video"
    }
}