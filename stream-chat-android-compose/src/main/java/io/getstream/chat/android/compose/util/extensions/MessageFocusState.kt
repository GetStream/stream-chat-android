package io.getstream.chat.android.compose.util.extensions

import io.getstream.chat.android.compose.state.messages.list.MessageFocusState
import io.getstream.chat.android.compose.state.messages.list.MessageFocusRemoved
import io.getstream.chat.android.compose.state.messages.list.MessageFocused
import io.getstream.chat.android.common.state.messagelist.MessageFocusState as MessageFocusStateCommon
import io.getstream.chat.android.common.state.messagelist.MessageFocusRemoved as MessageFocusRemovedCommon
import io.getstream.chat.android.common.state.messagelist.MessageFocused as MessageFocusedCommon

/**
 * Converts common [MessageFocusStateCommon] to compose [MessageFocusState].
 *
 * @return Compose [MessageFocusState] derived from [MessageFocusStateCommon].
 */
public fun MessageFocusStateCommon.toFocusState(): MessageFocusState {
    return when (this) {
        MessageFocusRemovedCommon -> MessageFocusRemoved
        MessageFocusedCommon -> MessageFocused
    }
}