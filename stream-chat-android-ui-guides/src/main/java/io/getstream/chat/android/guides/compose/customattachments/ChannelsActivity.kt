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

package io.getstream.chat.android.guides.compose.customattachments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.compose.ui.attachments.StreamAttachmentFactories
import io.getstream.chat.android.compose.ui.channels.ChannelsScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.guides.compose.customattachments.factory.dateAttachmentFactory

class ChannelsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val customFactories = listOf(dateAttachmentFactory)
        val defaultFactories = StreamAttachmentFactories.defaultFactories()

        setContent {
            ChatTheme(attachmentFactories = customFactories + defaultFactories) {
                ChannelsScreen(
                    onItemClick = { channel ->
                        startActivity(MessagesActivity.getIntent(this, channel.cid))
                    },
                    onBackPressed = { finish() }
                )
            }
        }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, ChannelsActivity::class.java)
        }
    }
}
