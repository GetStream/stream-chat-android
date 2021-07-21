package io.getstream.chat.android.compose.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Contains all the shapes we provide for our components.
 *
 * // TODO - define our shapes and apply them to components
 * @param avatar - The avatar shape.
 * @param myMessageBubble - The bubble that wraps message content.
 * @param inputField - The shape of the input field.
 * */
class StreamShapes(
    val avatar: Shape,
    val myMessageBubble: Shape,
    val otherMessageBubble: Shape,
    val inputField: Shape,
    val attachment: Shape,
    val bottomMenu: Shape
) {
    companion object {
        val default = StreamShapes(
            avatar = CircleShape,
            myMessageBubble = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp),
            otherMessageBubble = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp),
            inputField = RoundedCornerShape(24.dp),
            attachment = RoundedCornerShape(16.dp),
            bottomMenu = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        )
    }
}