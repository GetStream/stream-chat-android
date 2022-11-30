@file:Suppress("unused")

package io.getstream.chat.docs.kotlin.client.docusaurus

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.client.utils.Result
import java.io.File

/**
 * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/sending-custom-attachments/">Sending Attachments</a>
 */
class Attachments {

    /**
     * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/sending-custom-attachments/#sending-custom-attachments">Sending Custom Attachments</a>
     */
    fun sendAttachment() {
        val attachment = Attachment()
        val message = Message(
            cid = "messaging:general",
            text = "Look at this attachment!",
            attachments = mutableListOf(attachment),
        )
        ChatClient.instance().sendMessage(channelType = "messaging", channelId = "general", message = message)
            .enqueue { result ->
                when (result) {
                    is Result.Success -> {
                        // Handle success
                    }
                    is Result.Failure -> {
                        // Handle error
                    }
                }
            }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/sending-custom-attachments/#create-an-attachment-without-files">Create an Attachment Without Files#</a>
     */
    fun attachmentWithoutFile() {
        val attachment = Attachment(
            type = "location", // 1
            extraData = mutableMapOf( // 2
                "lat" to 40.017985,
                "lon" to -105.280184,
            ),
        )
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/sending-custom-attachments/#create-an-attachment-with-files">Create an Attachment With Files</a>
     */
    fun attachmentWithFile() {
        val attachment = Attachment(
            type = "audio", // 1
            upload = File("audio-file.mp3"), // 2
        )
    }
}
