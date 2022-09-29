/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.compose.util.extensions

import io.getstream.chat.android.compose.state.messages.SelectedMessageFailedModerationState
import io.getstream.chat.android.compose.state.messages.SelectedMessageOptionsState
import io.getstream.chat.android.compose.state.messages.SelectedMessageReactionsPickerState
import io.getstream.chat.android.compose.state.messages.SelectedMessageReactionsState
import io.getstream.chat.android.compose.state.messages.SelectedMessageState
import io.getstream.chat.android.common.state.messagelist.SelectedMessageFailedModerationState as SelectedMessageFailedModerationStateCommon
import io.getstream.chat.android.common.state.messagelist.SelectedMessageOptionsState as SelectedMessageOptionsStateCommon
import io.getstream.chat.android.common.state.messagelist.SelectedMessageReactionsPickerState as SelectedMessageReactionsPickerStateCommon
import io.getstream.chat.android.common.state.messagelist.SelectedMessageReactionsState as SelectedMessageReactionsStateCommon
import io.getstream.chat.android.common.state.messagelist.SelectedMessageState as SelectedMessageStateCommon

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
