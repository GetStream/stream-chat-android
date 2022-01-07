package io.getstream.chat.android.compose.previewdata

import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.models.Message
import java.util.Date

/**
 * Provides sample messages that will be used to render previews.
 */
internal object PreviewMessageData {

    val message1: Message = Message().apply {
        text = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit."
        createdAt = Date()
        type = ModelType.message_regular
    }

    val message2: Message = Message().apply {
        text = "Aenean commodo ligula eget dolor."
        createdAt = Date()
        type = ModelType.message_regular
    }
}
