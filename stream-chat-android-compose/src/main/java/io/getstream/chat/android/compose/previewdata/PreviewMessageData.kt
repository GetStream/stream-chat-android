package io.getstream.chat.android.compose.previewdata

import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import java.util.Date

/**
 * Provides sample messages that will be used to render previews.
 */
internal object PreviewMessageData {

    val message1: Message = Message().apply {
        id = "message-id-1"
        text = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit."
        createdAt = Date()
        type = ModelType.message_regular
    }

    val message2: Message = Message().apply {
        id = "message-id-2"
        text = "Aenean commodo ligula eget dolor."
        createdAt = Date()
        type = ModelType.message_regular
    }

    val messageWithOwnReaction: Message = Message().apply {
        id = "message-id-3"
        text = "Pellentesque leo dui, finibus et nibh et, congue aliquam lectus"
        createdAt = Date()
        type = ModelType.message_regular
        ownReactions = mutableListOf(Reaction(messageId = this.id, type = "haha"))
    }
}
