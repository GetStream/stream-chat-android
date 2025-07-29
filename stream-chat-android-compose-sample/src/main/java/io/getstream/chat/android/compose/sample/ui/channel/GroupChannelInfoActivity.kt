/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.sample.ui.channel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.sample.feature.channel.draft.DraftChannelActivity
import io.getstream.chat.android.compose.sample.ui.MessagesActivity
import io.getstream.chat.android.compose.sample.ui.channel.attachments.ChannelFilesAttachmentsActivity
import io.getstream.chat.android.compose.sample.ui.pinned.PinnedMessagesActivity
import io.getstream.chat.android.compose.ui.channel.info.GroupChannelInfoScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.channel.ChannelInfoViewModel
import io.getstream.chat.android.compose.viewmodel.channel.ChannelInfoViewModelFactory
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewEvent
import kotlinx.coroutines.flow.collectLatest

/**
 * Activity showing information about a group channel (chat).
 */
class GroupChannelInfoActivity : ComponentActivity() {

    companion object {
        private const val KEY_CHANNEL_ID = "channelId"

        /**
         * Creates an [Intent] for starting the [GroupChannelInfoActivity].
         *
         * @param context The calling [Context], used for building the [Intent].
         * @param channelId The ID of the channel for which the pinned messages are shown.
         */
        fun createIntent(context: Context, channelId: String) =
            Intent(context, GroupChannelInfoActivity::class.java)
                .putExtra(KEY_CHANNEL_ID, channelId)
    }

    private val channelId by lazy { requireNotNull(intent.getStringExtra(KEY_CHANNEL_ID)) }

    private val viewModelFactory by lazy {
        ChannelInfoViewModelFactory(
            context = applicationContext,
            cid = channelId,
        )
    }
    private val viewModel by viewModels<ChannelInfoViewModel> { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var showAddMembers by remember { mutableStateOf(false) }
            ChatTheme {
                GroupChannelInfoScreen(
                    modifier = Modifier.statusBarsPadding(),
                    viewModelFactory = viewModelFactory,
                    onNavigationIconClick = ::finish,
                    onAddMembersClick = { showAddMembers = true },
                )
                if (showAddMembers) {
                    AddMembersDialog(
                        cid = channelId,
                        onDismiss = { showAddMembers = false },
                    )
                }
            }
            LaunchedEffect(viewModel) {
                viewModel.events.collectLatest { event ->
                    when (event) {
                        is ChannelInfoViewEvent.Error -> showError(event)
                        is ChannelInfoViewEvent.Navigation -> onNavigationEvent(event)
                        is ChannelInfoViewEvent.Modal -> Unit
                    }
                }
            }
        }
    }

    private fun onNavigationEvent(event: ChannelInfoViewEvent.Navigation) {
        when (event) {
            is ChannelInfoViewEvent.NavigateUp -> {
                setResult(RESULT_OK)
                finish()
            }

            is ChannelInfoViewEvent.NavigateToPinnedMessages ->
                openPinnedMessages()

            ChannelInfoViewEvent.NavigateToMediaAttachments ->
                openMediaAttachments()

            ChannelInfoViewEvent.NavigateToFilesAttachments ->
                openFilesAttachments()

            is ChannelInfoViewEvent.NavigateToChannel ->
                startActivity(MessagesActivity.createIntent(context = this, channelId = event.cid))

            is ChannelInfoViewEvent.NavigateToDraftChannel ->
                startActivity(DraftChannelActivity.createIntent(context = this, memberIds = listOf(event.memberId)))
        }
    }

    private fun openPinnedMessages() {
        val intent = PinnedMessagesActivity.createIntent(
            context = this,
            channelId = channelId,
        )
        startActivity(intent)
    }

    private fun openMediaAttachments() {
        // TODO Implement the logic to open media attachments screen
        Toast.makeText(this, "Media Attachments", Toast.LENGTH_SHORT).show()
    }

    private fun openFilesAttachments() {
        val intent = ChannelFilesAttachmentsActivity.createIntent(
            context = this,
            cid = channelId,
        )
        startActivity(intent)
    }

    private fun showError(error: ChannelInfoViewEvent.Error) {
        val message = when (error) {
            ChannelInfoViewEvent.RenameChannelError,
            -> R.string.stream_ui_channel_info_rename_group_error

            ChannelInfoViewEvent.MuteChannelError,
            ChannelInfoViewEvent.UnmuteChannelError,
            -> R.string.stream_ui_channel_info_mute_group_error

            ChannelInfoViewEvent.HideChannelError,
            ChannelInfoViewEvent.UnhideChannelError,
            -> R.string.stream_ui_channel_info_hide_group_error

            ChannelInfoViewEvent.LeaveChannelError,
            -> R.string.stream_ui_channel_info_leave_group_error

            ChannelInfoViewEvent.DeleteChannelError,
            -> R.string.stream_ui_channel_info_delete_group_error

            ChannelInfoViewEvent.BanMemberError,
            -> R.string.stream_ui_channel_info_ban_member_error

            ChannelInfoViewEvent.UnbanMemberError,
            -> R.string.stream_ui_channel_info_unban_member_error

            ChannelInfoViewEvent.RemoveMemberError,
            -> R.string.stream_ui_channel_info_remove_member_error
        }
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}
