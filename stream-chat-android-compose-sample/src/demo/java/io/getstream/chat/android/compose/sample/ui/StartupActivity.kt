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

package io.getstream.chat.android.compose.sample.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.TaskStackBuilder
import androidx.lifecycle.lifecycleScope
import io.getstream.chat.android.compose.sample.BuildConfig
import io.getstream.chat.android.compose.sample.ChatApp
import io.getstream.chat.android.compose.sample.ChatHelper
import io.getstream.chat.android.compose.sample.feature.channel.list.ChannelsActivity
import io.getstream.chat.android.compose.sample.ui.login.UserLoginActivity
import kotlinx.coroutines.launch

/**
 * An Activity without UI responsible for startup routing. It navigates the user to
 * one of the following screens:
 *
 * - Login screen, if the user is not authenticated
 * - Channels screen, if the user is authenticated
 * - Messages screen, if the user is coming from a push notification
 */
class StartupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val userCredentials = ChatApp.credentialsRepository.loadUserCredentials()
            if (userCredentials != null && !BuildConfig.BENCHMARK) {
                // Ensure that the user is connected
                ChatHelper.connectUser(userCredentials)

                if (intent.hasExtra(KEY_CHANNEL_ID)) {
                    // Navigating from push, route to the messages screen
                    val channelId = requireNotNull(intent.getStringExtra(KEY_CHANNEL_ID))
                    val messageId = intent.getStringExtra(KEY_MESSAGE_ID)
                    val parentMessageId = intent.getStringExtra(KEY_PARENT_MESSAGE_ID)

                    TaskStackBuilder.create(this@StartupActivity)
                        .addNextIntent(ChannelsActivity.createIntent(this@StartupActivity))
                        .addNextIntent(
                            MessagesActivity.createIntent(
                                context = this@StartupActivity,
                                channelId = channelId,
                                messageId = messageId,
                                parentMessageId = parentMessageId,
                            ),
                        )
                        .startActivities()
                } else {
                    // Logged in, navigate to the channels screen
                    startActivity(ChannelsActivity.createIntent(this@StartupActivity))
                }
            } else {
                // Not logged in, start with the login screen
                startActivity(UserLoginActivity.createIntent(this@StartupActivity))
            }
            finish()
        }
    }

    companion object {
        private const val KEY_CHANNEL_ID = "channelId"
        private const val KEY_MESSAGE_ID = "messageId"
        private const val KEY_PARENT_MESSAGE_ID = "parentMessageId"

        fun createIntent(
            context: Context,
            channelId: String,
            messageId: String?,
            parentMessageId: String?,
        ): Intent {
            return Intent(context, StartupActivity::class.java).apply {
                putExtra(KEY_CHANNEL_ID, channelId)
                putExtra(KEY_MESSAGE_ID, messageId)
                putExtra(KEY_PARENT_MESSAGE_ID, parentMessageId)
            }
        }
    }
}
