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
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import io.getstream.chat.android.compose.ui.components.reactions.ReactionIconSize
import io.getstream.chat.android.compose.ui.messages.MessagesScreen
import io.getstream.chat.android.compose.ui.theme.ChatComponentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.ReactionResolver
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
            // Pass your custom implementations to ChatTheme
            ChatTheme(
                reactionResolver = CustomReactionResolver(),
                componentFactory = CustomComponentFactory(),
            ) {
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

/**
 * Implement [ReactionResolver] if you want to customize which reactions are available
 * and what emoji each type maps to.
 */
class CustomReactionResolver : ReactionResolver {

    override val supportedReactions: Set<String> = setOf(
        THUMBS_UP,
        THUMBS_DOWN,
        MOOD_GOOD,
        MOOD_BAD,
    )

    override fun emojiCode(type: String): String? = when (type) {
        THUMBS_UP -> "👍"
        THUMBS_DOWN -> "👎"
        MOOD_GOOD -> "🙂"
        MOOD_BAD -> "😞"
        else -> null
    }

    companion object {
        private const val THUMBS_UP: String = "thumbs_up"
        private const val THUMBS_DOWN: String = "thumbs_down"
        private const val MOOD_GOOD: String = "mood_good"
        private const val MOOD_BAD: String = "mood_bad"
    }
}

/**
 * Implement [ChatComponentFactory] if you want to render something other than emojis for specific
 * reaction types. For types that should still use emojis, you can delegate to `super`.
 */
class CustomComponentFactory : ChatComponentFactory {

    @Composable
    override fun ReactionIcon(
        type: String,
        emoji: String?,
        size: ReactionIconSize,
        modifier: Modifier,
    ) {
        if (type == "mood_good") {
            Image(
                painter = painterResource(R.drawable.ic_mood_good),
                contentDescription = type,
                modifier = modifier,
            )
        } else {
            super.ReactionIcon(type, emoji, size, modifier)
        }
    }
}
