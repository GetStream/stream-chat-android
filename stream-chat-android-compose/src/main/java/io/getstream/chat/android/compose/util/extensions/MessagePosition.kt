package io.getstream.chat.android.compose.util.extensions

import io.getstream.chat.android.common.state.messagelist.MessagePosition
import io.getstream.chat.android.compose.state.messages.list.MessageItemGroupPosition

/**
 * Converts common [MessagePosition] to compose [MessageItemGroupPosition].
 *
 * @return Derived [MessageItemGroupPosition] from [MessagePosition].
 */
public fun MessagePosition.toMessageItemGroupPosition(): MessageItemGroupPosition {
    return when(this) {
        MessagePosition.TOP -> MessageItemGroupPosition.Top
        MessagePosition.MIDDLE -> MessageItemGroupPosition.Middle
        MessagePosition.BOTTOM -> MessageItemGroupPosition.Bottom
        MessagePosition.NONE -> MessageItemGroupPosition.None
    }
}