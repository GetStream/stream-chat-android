package io.getstream.chat.android.compose.util.extensions

import io.getstream.chat.android.compose.state.messages.SelectedMessageFailedModerationState
import io.getstream.chat.android.compose.state.messages.SelectedMessageOptionsState
import io.getstream.chat.android.compose.state.messages.SelectedMessageReactionsPickerState
import io.getstream.chat.android.compose.state.messages.SelectedMessageReactionsState
import io.getstream.chat.android.compose.state.messages.SelectedMessageState
import io.getstream.chat.android.common.state.messagelist.SelectedMessageState as SelectedMessageStateCommon
import io.getstream.chat.android.common.state.messagelist.SelectedMessageFailedModerationState as SelectedMessageFailedModerationStateCommon
import io.getstream.chat.android.common.state.messagelist.SelectedMessageOptionsState as SelectedMessageOptionsStateCommon
import io.getstream.chat.android.common.state.messagelist.SelectedMessageReactionsPickerState as SelectedMessageReactionsPickerStateCommon
import io.getstream.chat.android.common.state.messagelist.SelectedMessageReactionsState as SelectedMessageReactionsStateCommon

/**
 * Converts [SelectedMessageStateCommon] to compose [SelectedMessageState].
 *
 * @return Derived compose [SelectedMessageState] from [SelectedMessageStateCommon].
 */
public fun SelectedMessageStateCommon.toComposeState(): SelectedMessageState {
    return when (this) {
        is SelectedMessageFailedModerationStateCommon -> SelectedMessageFailedModerationState(message, ownCapabilities)
        is SelectedMessageOptionsStateCommon -> SelectedMessageOptionsState(message, ownCapabilities)
        is SelectedMessageReactionsPickerStateCommon -> SelectedMessageReactionsPickerState(message, ownCapabilities)
        is SelectedMessageReactionsStateCommon -> SelectedMessageReactionsState(message, ownCapabilities)
    }
}