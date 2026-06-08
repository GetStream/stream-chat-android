/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.state.channels.list

import io.getstream.result.Error

/**
 * One-off events emitted by the channel list in response to user-triggered actions, used to drive transient feedback
 * such as a snackbar.
 */
internal sealed interface ChannelListEvent {

    /**
     * A user-triggered channel action failed.
     *
     * @param action The action that failed, used to pick the feedback message.
     * @param error The error that caused the failure.
     */
    data class ActionError(val action: ChannelListAction, val error: Error) : ChannelListEvent

    /**
     * A channel was successfully deleted.
     */
    data object ChannelDeleted : ChannelListEvent
}

/**
 * The user-triggered channel actions that can produce a [ChannelListEvent.ActionError].
 */
internal enum class ChannelListAction {
    MuteChannel,
    UnmuteChannel,
    PinChannel,
    UnpinChannel,
    ArchiveChannel,
    UnarchiveChannel,
    DeleteChannel,
    LeaveGroup,
    MuteUser,
    UnmuteUser,
    BlockUser,
    UnblockUser,
}
