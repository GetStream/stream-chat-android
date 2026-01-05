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

package io.getstream.chat.android.uitests.app.compose

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.res.stringResource
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.ui.channels.ChannelsScreen
import io.getstream.chat.android.compose.ui.channels.SearchMode
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.uitests.R
import io.getstream.chat.android.uitests.app.login.LoginActivity

/**
 * An Activity that represents a channel list screen. Relies on the components
 * taken from Compose SDK.
 */
class ComposeChannelsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ChatTheme {
                ChannelsScreen(
                    title = stringResource(id = R.string.sdk_name_compose),
                    searchMode = SearchMode.Channels,
                    onChannelClick = ::openMessages,
                    onHeaderAvatarClick = ::logout,
                    onBackPressed = ::finish,
                )
            }
        }
    }

    /**
     * Navigates to the message list screen.
     *
     * @param channel The channel to open.
     */
    private fun openMessages(channel: Channel) {
        startActivity(ComposeMessagesActivity.createIntent(this, channel.cid))
    }

    /**
     * Logs out and navigated to the login screen.
     */
    private fun logout() {
        ChatClient.instance().disconnect(flushPersistence = false).enqueue {
            finish()
            startActivity(LoginActivity.createIntent(this))
            overridePendingTransition(0, 0)
        }
    }

    companion object {
        /**
         * Create an [Intent] to start [ComposeChannelsActivity].
         *
         * @param context The context used to create the intent.
         * @return The [Intent] to start [ComposeChannelsActivity].
         */
        fun createIntent(context: Context): Intent {
            return Intent(context, ComposeChannelsActivity::class.java)
        }
    }
}
