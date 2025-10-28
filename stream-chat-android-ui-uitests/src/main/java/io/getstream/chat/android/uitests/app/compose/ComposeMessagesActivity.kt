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

package io.getstream.chat.android.uitests.app.compose

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.compose.ui.messages.MessagesScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory

/**
 * An Activity that represents a message list screen. Relies on the components
 * taken from Compose SDK.
 */
class ComposeMessagesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val channelId = requireNotNull(intent.getStringExtra(KEY_CHANNEL_ID))

        setContent {
            ChatTheme {
                MessagesScreen(
                    viewModelFactory = MessagesViewModelFactory(
                        context = this,
                        channelId = channelId,
                    ),
                    onBackPressed = ::finish,
                )
            }
        }
    }

    companion object {
        private const val KEY_CHANNEL_ID = "channelId"

        /**
         * Create an [Intent] to start [ComposeMessagesActivity].
         *
         * @param context The context used to create the intent.
         * @param channelId The channel id. ie 123.
         * @return The [Intent] to start [ComposeMessagesActivity].
         */
        fun createIntent(context: Context, channelId: String): Intent = Intent(context, ComposeMessagesActivity::class.java).apply {
            putExtra(KEY_CHANNEL_ID, channelId)
        }
    }
}
