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

import io.getstream.chat.android.compose.state.messages.list.MessageFocusRemoved
import io.getstream.chat.android.compose.state.messages.list.MessageFocusState
import io.getstream.chat.android.compose.state.messages.list.MessageFocused
import io.getstream.chat.android.common.state.messagelist.MessageFocusRemoved as MessageFocusRemovedCommon
import io.getstream.chat.android.common.state.messagelist.MessageFocusState as MessageFocusStateCommon
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
