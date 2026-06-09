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

package io.getstream.chat.android.compose.ui.channels

import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.channels.list.ChannelListAction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ChannelListActionMessagesTest {

    @Test
    fun `Every channel action maps to its own error message`() {
        val expected = mapOf(
            ChannelListAction.MuteChannel to R.string.stream_compose_channel_list_action_error_mute_channel,
            ChannelListAction.UnmuteChannel to R.string.stream_compose_channel_list_action_error_unmute_channel,
            ChannelListAction.PinChannel to R.string.stream_compose_channel_list_action_error_pin_channel,
            ChannelListAction.UnpinChannel to R.string.stream_compose_channel_list_action_error_unpin_channel,
            ChannelListAction.ArchiveChannel to R.string.stream_compose_channel_list_action_error_archive_channel,
            ChannelListAction.UnarchiveChannel to R.string.stream_compose_channel_list_action_error_unarchive_channel,
            ChannelListAction.DeleteChannel to R.string.stream_compose_channel_list_action_error_delete_channel,
            ChannelListAction.LeaveGroup to R.string.stream_compose_channel_list_action_error_leave_group,
            ChannelListAction.MuteUser to R.string.stream_compose_channel_list_action_error_mute_user,
            ChannelListAction.UnmuteUser to R.string.stream_compose_channel_list_action_error_unmute_user,
            ChannelListAction.BlockUser to R.string.stream_compose_channel_list_action_error_block_user,
            ChannelListAction.UnblockUser to R.string.stream_compose_channel_list_action_error_unblock_user,
        )

        ChannelListAction.entries.forEach { action ->
            assertEquals(expected[action], action.errorMessageResId(), "Unexpected message for $action")
        }
    }
}
