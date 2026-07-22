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

package io.getstream.chat.android.compose.sample.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.TaskStackBuilder
import androidx.lifecycle.lifecycleScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.sample.ChatHelper
import io.getstream.chat.android.compose.sample.data.PredefinedUserCredentials
import io.getstream.chat.android.compose.sample.data.customSettings
import io.getstream.chat.android.compose.sample.feature.channel.list.ChannelsActivity
import io.getstream.chat.android.compose.sample.ui.channel.ChannelActivity
import kotlinx.coroutines.launch

class StartupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            // The harness always launches with BASE_URL; a launch without it comes from a
            // notification tap, where the already initialized client must stay untouched.
            val baseUrl = intent.getStringExtra("BASE_URL")
            if (baseUrl != null) {
                // The process survives across retry attempts, so disconnect the client left by
                // the previous attempt before building a new one.
                ChatClient.instance().disconnect(flushPersistence = true).await()
                ChatHelper.initializeSdk(applicationContext, PredefinedUserCredentials.API_KEY, baseUrl)
                customSettings().isComposerLinkPreviewEnabled = true
            }

            val initTestActivity = intent.getSerializableExtra("InitTestActivity") as? InitTestActivity
            if (initTestActivity != null) {
                startActivity(initTestActivity.createIntent(this@StartupActivity))
            } else {
                // Navigating from a push notification, route to the messages screen
                val channelId = requireNotNull(intent.getStringExtra(KEY_CHANNEL_ID))
                TaskStackBuilder.create(applicationContext)
                    .addNextIntent(ChannelsActivity.createIntent(applicationContext))
                    .addNextIntent(
                        ChannelActivity.createIntent(
                            context = applicationContext,
                            channelId = channelId,
                            messageId = intent.getStringExtra(KEY_MESSAGE_ID),
                            parentMessageId = intent.getStringExtra(KEY_PARENT_MESSAGE_ID),
                        ),
                    )
                    .startActivities()
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
            return Intent(context, StartupActivity::class.java)
                .putExtra(KEY_CHANNEL_ID, channelId)
                .putExtra(KEY_MESSAGE_ID, messageId)
                .putExtra(KEY_PARENT_MESSAGE_ID, parentMessageId)
        }
    }
}
