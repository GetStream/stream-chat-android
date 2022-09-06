package io.getstream.chat.android.ui.utils.extensions

import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.common.state.messagelist.MessagePosition

/**
 * Converts the common [MessagePosition] to ui-components [MessageListItem.Position].
 *
 * @return [MessageListItem.Position] derived from [MessagePosition].
 */
public fun MessagePosition.toUiPosition(): MessageListItem.Position? {
    return when(this) {
        MessagePosition.TOP -> MessageListItem.Position.TOP
        MessagePosition.MIDDLE -> MessageListItem.Position.MIDDLE
        MessagePosition.BOTTOM -> MessageListItem.Position.BOTTOM
        MessagePosition.NONE -> null
    }
}

/**
 * Converts the ui-components [MessageListItem.Position] to common [MessagePosition].
 *
 * @return [MessagePosition] derived from [MessageListItem.Position].
 */
public fun MessageListItem.Position.toCommonPosition(): MessagePosition {
    return when(this) {
        MessageListItem.Position.TOP -> MessagePosition.TOP
        MessageListItem.Position.MIDDLE -> MessagePosition.MIDDLE
        MessageListItem.Position.BOTTOM -> MessagePosition.BOTTOM
    }
}