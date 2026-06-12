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

import androidx.annotation.StringRes
import io.getstream.chat.android.compose.R

/**
 * One-off events emitted by the channel list in response to user-triggered actions, used to drive transient feedback
 * such as a snackbar.
 */
internal sealed interface ChannelListEvent {

    /**
     * A user-triggered channel action failed.
     *
     * @param action The action that failed, used to pick the feedback message.
     */
    data class ActionError(val action: ChannelListAction) : ChannelListEvent

    /**
     * A channel was successfully deleted.
     */
    data object ChannelDeleted : ChannelListEvent
}

/**
 * The user-triggered channel actions that can produce a [ChannelListEvent.ActionError].
 *
 * @param errorMessageResId The message shown when the action fails.
 */
internal enum class ChannelListAction(@get:StringRes val errorMessageResId: Int) {
    MuteChannel(R.string.stream_compose_channel_list_action_error_mute_channel),
    UnmuteChannel(R.string.stream_compose_channel_list_action_error_unmute_channel),
    PinChannel(R.string.stream_compose_channel_list_action_error_pin_channel),
    UnpinChannel(R.string.stream_compose_channel_list_action_error_unpin_channel),
    ArchiveChannel(R.string.stream_compose_channel_list_action_error_archive_channel),
    UnarchiveChannel(R.string.stream_compose_channel_list_action_error_unarchive_channel),
    DeleteChannel(R.string.stream_compose_channel_list_action_error_delete_channel),
    LeaveGroup(R.string.stream_compose_channel_list_action_error_leave_group),
    MuteUser(R.string.stream_compose_channel_list_action_error_mute_user),
    UnmuteUser(R.string.stream_compose_channel_list_action_error_unmute_user),
    BlockUser(R.string.stream_compose_channel_list_action_error_block_user),
    UnblockUser(R.string.stream_compose_channel_list_action_error_unblock_user),
}
