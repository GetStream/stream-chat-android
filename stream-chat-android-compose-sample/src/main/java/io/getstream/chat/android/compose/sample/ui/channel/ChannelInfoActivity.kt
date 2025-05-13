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
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.sample.ui.BaseConnectedActivity
import io.getstream.chat.android.compose.sample.ui.pinned.PinnedMessagesActivity
import io.getstream.chat.android.compose.ui.channel.info.DirectChannelInfoScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.channel.ChannelInfoViewModel
import io.getstream.chat.android.compose.viewmodel.channel.ChannelInfoViewModelFactory
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewEvent
import kotlinx.coroutines.flow.collectLatest

/**
 * Activity showing information about a 1-to-1 channel (chat).
 */
class ChannelInfoActivity : BaseConnectedActivity() {

    companion object {
        private const val KEY_CHANNEL_ID = "channelId"

        /**
         * Creates an [Intent] for starting the [ChannelInfoActivity].
         *
         * @param context The calling [Context], used for building the [Intent].
         * @param channelId The ID of the channel for which the pinned messages are shown.
         */
        fun createIntent(context: Context, channelId: String) =
            Intent(context, ChannelInfoActivity::class.java)
                .putExtra(KEY_CHANNEL_ID, channelId)
    }

    private val viewModelFactory by lazy {
        ChannelInfoViewModelFactory(
            context = applicationContext,
            cid = requireNotNull(intent.getStringExtra(KEY_CHANNEL_ID)),
        )
    }

    private val viewModel by viewModels<ChannelInfoViewModel> { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatTheme {
                DirectChannelInfoScreen(
                    modifier = Modifier.statusBarsPadding(),
                    viewModelFactory = viewModelFactory,
                    onNavigationIconClick = ::finish,
                    onPinnedMessagesClick = ::openPinnedMessages,
                )
            }
            LaunchedEffect(Unit) {
                viewModel.events.collectLatest { event ->
                    when (event) {
                        is ChannelInfoViewEvent.Error ->
                            showError(event)

                        is ChannelInfoViewEvent.HideChannelSuccess,
                        is ChannelInfoViewEvent.LeaveChannelSuccess,
                        is ChannelInfoViewEvent.DeleteChannelSuccess,
                            -> {
                            setResult(RESULT_OK)
                            finish()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun openPinnedMessages() {
        val intent = PinnedMessagesActivity.createIntent(
            context = this,
            channelId = requireNotNull(intent.getStringExtra(KEY_CHANNEL_ID)),
        )
        startActivity(intent)
    }

    private fun showError(error: ChannelInfoViewEvent.Error) {
        val message = when (error) {
            ChannelInfoViewEvent.RenameChannelError -> R.string.stream_ui_channel_info_rename_group_error

            ChannelInfoViewEvent.MuteChannelError,
            ChannelInfoViewEvent.UnmuteChannelError,
                -> R.string.stream_ui_channel_info_option_mute_conversation_error

            ChannelInfoViewEvent.HideChannelError,
            ChannelInfoViewEvent.UnhideChannelError,
                -> R.string.stream_ui_channel_info_option_hide_conversation_error

            ChannelInfoViewEvent.LeaveChannelError -> R.string.stream_ui_channel_info_option_leave_conversation_error
            ChannelInfoViewEvent.DeleteChannelError -> R.string.stream_ui_channel_info_option_delete_conversation_error
        }
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}
