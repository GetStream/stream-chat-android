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

package io.getstream.chat.android.guides.catalog.compose.customreactions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import io.getstream.chat.android.compose.ui.messages.MessagesScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.ReactionIcon
import io.getstream.chat.android.compose.ui.util.ReactionIconFactory
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.guides.R

/**
 * An Activity representing a self-contained chat screen with custom reaction icons.
 */
class MessagesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val channelId = requireNotNull(intent.getStringExtra(KEY_CHANNEL_ID))

        setContent {
            // Pass your factory to ChatTheme here
            ChatTheme(reactionIconFactory = CustomReactionIconFactory()) {
                MessagesScreen(
                    viewModelFactory = MessagesViewModelFactory(
                        context = this,
                        channelId = channelId,
                        threadLoadOlderToNewer = true,
                    ),
                    onBackPressed = { finish() },
                    onHeaderTitleClick = {},
                )
            }
        }
    }

    companion object {
        private const val KEY_CHANNEL_ID = "channelId"

        fun createIntent(context: Context, channelId: String): Intent {
            return Intent(context, MessagesActivity::class.java).apply {
                putExtra(KEY_CHANNEL_ID, channelId)
            }
        }
    }
}

class CustomReactionIconFactory : ReactionIconFactory {

    override fun isReactionSupported(type: String): Boolean {
        return supportedReactions.contains(type)
    }

    @Composable
    override fun createReactionIcon(type: String): ReactionIcon {
        return when (type) {
            THUMBS_UP -> ReactionIcon(
                painter = painterResource(R.drawable.ic_thumb_up),
                selectedPainter = painterResource(R.drawable.ic_thumb_up_selected),
            )
            THUMBS_DOWN -> ReactionIcon(
                painter = painterResource(R.drawable.ic_thumb_down),
                selectedPainter = painterResource(R.drawable.ic_thumb_down_selected),
            )
            MOOD_GOOD -> ReactionIcon(
                painter = painterResource(R.drawable.ic_mood_good),
                selectedPainter = painterResource(R.drawable.ic_mood_good_selected),
            )
            MOOD_BAD -> ReactionIcon(
                painter = painterResource(R.drawable.ic_mood_bad),
                selectedPainter = painterResource(R.drawable.ic_mood_bad_selected),
            )
            else -> throw IllegalArgumentException("Unsupported reaction type")
        }
    }

    @Composable
    override fun createReactionIcons(): Map<String, ReactionIcon> {
        return supportedReactions.associateWith { createReactionIcon(it) }
    }

    companion object {
        private const val THUMBS_UP: String = "thumbs_up"
        private const val THUMBS_DOWN: String = "thumbs_down"
        private const val MOOD_GOOD: String = "mood_good"
        private const val MOOD_BAD: String = "mood_bad"

        private val supportedReactions = setOf(
            THUMBS_UP,
            THUMBS_DOWN,
            MOOD_GOOD,
            MOOD_BAD,
        )
    }
}
