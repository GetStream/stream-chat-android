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

package io.getstream.chat.android.guides.catalog.compose.customizingimageandvideoattachments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.messages.MessagesScreen
import io.getstream.chat.android.compose.ui.theme.ChatComponentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageComposerAttachmentMediaItemOverlayParams
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.guides.catalog.compose.customizingimageandvideoattachments.ui.CustomPlayButton
import io.getstream.chat.android.models.AttachmentType

private const val PlayButtonWidthFraction = 0.35f
private const val PlayButtonAspectRatio = 1.20f

/**
 * An activity featuring a fully functional message list screen with a custom
 * media attachment preview in the composer.
 */
class MessagesActivity : ComponentActivity() {

    private val messagesViewModelFactory by lazy {
        MessagesViewModelFactory(
            context = this,
            requireNotNull(intent.getStringExtra(KEY_CHANNEL_ID)),
            threadLoadOlderToNewer = true,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ChatTheme(componentFactory = CustomMediaChatComponentFactory) {
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

/**
 * A custom [ChatComponentFactory] that renders a custom play button overlay
 * above video attachments in the message composer preview.
 *
 * To customize the overlay in the message list, override [MediaAttachmentContent].
 */
object CustomMediaChatComponentFactory : ChatComponentFactory {

    @Composable
    override fun MessageComposerAttachmentMediaItemOverlay(
        params: MessageComposerAttachmentMediaItemOverlayParams,
    ) {
        if (params.attachmentType == AttachmentType.VIDEO) {
            CustomPlayButton(
                modifier = Modifier
                    .padding(2.dp)
                    .background(
                        color = Color(red = 255, blue = 255, green = 255, alpha = 220),
                        shape = RoundedCornerShape(8.dp),
                    )
                    .fillMaxWidth(PlayButtonWidthFraction)
                    .aspectRatio(PlayButtonAspectRatio),
            )
        }
    }
}
