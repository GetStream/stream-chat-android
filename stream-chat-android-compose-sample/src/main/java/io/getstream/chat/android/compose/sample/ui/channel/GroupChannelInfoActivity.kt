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
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import io.getstream.chat.android.compose.sample.ui.BaseConnectedActivity
import io.getstream.chat.android.compose.sample.ui.pinned.PinnedMessagesActivity
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Activity showing information about a group channel (chat).
 */
class GroupChannelInfoActivity : BaseConnectedActivity() {

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

    private val viewModelFactory by lazy {
        GroupChannelInfoViewModelFactory(
            cid = requireNotNull(intent.getStringExtra(KEY_CHANNEL_ID)),
        )
    }
    private val viewModel by viewModels<GroupChannelInfoViewModel>(factoryProducer = { viewModelFactory })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatTheme {
                val state by viewModel.state.collectAsState()
                GroupChannelInfoScreen(
                    modifier = Modifier.statusBarsPadding(),
                    state = state,
                    onNavigationIconClick = ::finish,
                    onPinnedMessagesClick = ::openPinnedMessages,
                )
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
}
