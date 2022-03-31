@file:Suppress("unused")

package io.getstream.chat.docs.kotlin.client.docusaurus

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import java.io.File

class Attachments {

    fun sendAttachment() {
        val attachment = Attachment()
        val message = Message(
            cid = "messaging:general",
            text = "Look at this attachment!",
            attachments = mutableListOf(attachment),
        )
        ChatClient.instance().sendMessage(channelType = "messaging", channelId = "general", message = message)
            .enqueue { result ->
                if (result.isSuccess) {
                    // Use result.data()
                } else {
                    // Handle result.error()
                }
            }
    }

    fun attachmentWithoutFile() {
        val attachment = Attachment(
            type = "location", // 1
            extraData = mutableMapOf( // 2
                "lat" to 40.017985,
                "lon" to -105.280184,
            ),
        )
    }

    fun attachmentWithFile() {
        val attachment = Attachment(
            type = "audio", // 1
            upload = File("audio-file.mp3"), // 2
        )
    }
}
