package io.getstream.chat.android.compose.util.extensions

import io.getstream.chat.android.compose.state.messages.MyOwn
import io.getstream.chat.android.common.state.messagelist.NewMessageState as NewMessageStateCommon
import io.getstream.chat.android.common.state.messagelist.MyOwn as MyOwnCommon
import io.getstream.chat.android.common.state.messagelist.Other as OtherCommon
import io.getstream.chat.android.compose.state.messages.NewMessageState
import io.getstream.chat.android.compose.state.messages.Other

/**
 * Converts [NewMessageStateCommon] to compose [NewMessageState].
 *
 * @return Composer [NewMessageState] derived from [NewMessageStateCommon].
 */
public fun NewMessageStateCommon.toComposeState(): NewMessageState {
    return when (this) {
        MyOwnCommon -> MyOwn
        OtherCommon -> Other
    }
}