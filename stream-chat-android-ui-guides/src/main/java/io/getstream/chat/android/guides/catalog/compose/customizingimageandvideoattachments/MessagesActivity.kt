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

package io.getstream.chat.android.guides.catalog.compose.customizingimageandvideoattachments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.getstream.chat.android.compose.ui.attachments.StreamAttachmentFactories
import io.getstream.chat.android.compose.ui.attachments.factory.FileAttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.factory.GiphyAttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.factory.LinkAttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.factory.UnsupportedAttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.factory.UploadAttachmentFactory
import io.getstream.chat.android.compose.ui.messages.MessagesScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.guides.catalog.compose.customizingimageandvideoattachments.factory.customMediaAttachmentFactory

/**
 * An activity featuring a fully functional message list screen with a custom
 * media attachment factory.
 */
class MessagesActivity : ComponentActivity() {

    private val messagesViewModelFactory by lazy {
        MessagesViewModelFactory(
            context = this,
            requireNotNull(intent.getStringExtra(KEY_CHANNEL_ID)),
            threadLoadOlderToNewer = true,
        )
    }

    /**
     * A list of attachment factories that mimics the order of those
     * found in [StreamAttachmentFactories.defaults] while
     * replacing the default media attachment factory with a
     * custom one.
     */
    private val attachmentFactories = listOf(
        UploadAttachmentFactory(),
        LinkAttachmentFactory(linkDescriptionMaxLines = 5),
        GiphyAttachmentFactory(),
        customMediaAttachmentFactory,
        FileAttachmentFactory(),
        UnsupportedAttachmentFactory,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ChatTheme(attachmentFactories = attachmentFactories) {
                MessagesScreen(
                    viewModelFactory = messagesViewModelFactory,
                    onBackPressed = { finish() },
                )
            }
        }
    }

    companion object {
        private const val KEY_CHANNEL_ID = "channelId"

        /**
         * Creates an [Intent] to start [MessagesActivity].
         *
         * @param context The context used to create the intent.
         * @param channelId The id of the channel.
         * @return The [Intent] to start [MessagesActivity].
         */
        fun getIntent(context: Context, channelId: String): Intent {
            return Intent(context, MessagesActivity::class.java).apply {
                putExtra(KEY_CHANNEL_ID, channelId)
            }
        }
    }
}
