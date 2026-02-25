// ktlint-disable filename

package io.getstream.chat.docs.kotlin.compose.guides

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.reactions.ReactionIconSize
import io.getstream.chat.android.compose.ui.messages.MessagesScreen
import io.getstream.chat.android.compose.ui.theme.ChatComponentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.ReactionResolver
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory

/**
 * [Providing Custom Reactions](https://getstream.io/chat/docs/sdk/android/compose/guides/providing-custom-reactions/)
 */
private object ProvidingCustomReactionsSnippet {

    /**
     * Implement [ReactionResolver] to customize which reactions are available and what emoji each
     * type maps to. Implement [ChatComponentFactory] if you want to render non-emoji reactions for
     * specific reaction types. Pass both to [ChatTheme].
     */
    class CustomReactionsActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            val channelId = requireNotNull(intent.getStringExtra(KEY_CHANNEL_ID))

            setContent {
                ChatTheme(
                    reactionResolver = CustomReactionResolver(),
                    componentFactory = ImageReactionComponentFactory(),
                ) {
                    MessagesScreen(
                        viewModelFactory = MessagesViewModelFactory(
                            context = this,
                            channelId = channelId,
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
                return Intent(context, CustomReactionsActivity::class.java).apply {
                    putExtra(KEY_CHANNEL_ID, channelId)
                }
            }
        }
    }

    /** Custom resolver that provides emoji codes for custom reaction types. */
    class CustomReactionResolver : ReactionResolver {

        override val supportedReactions: Set<String> = linkedSetOf(
            THUMBS_UP,
            THUMBS_DOWN,
            MOOD_GOOD,
            MOOD_BAD,
            HAHA,
        )

        // Here we return only the reactions that should show up in quick access
        override val defaultReactions: List<String> = listOf(THUMBS_UP, THUMBS_DOWN, HAHA)

        override fun emojiCode(type: String): String? = when (type) {
            THUMBS_UP -> "👍"
            THUMBS_DOWN -> "👎"
            MOOD_GOOD -> "😀"
            MOOD_BAD -> "😞"
            HAHA -> "😂"
            else -> null
        }

        companion object {
            const val THUMBS_UP: String = "thumbs_up"
            const val THUMBS_DOWN: String = "thumbs_down"
            const val MOOD_GOOD: String = "mood_good"
            const val MOOD_BAD: String = "mood_bad"
            const val HAHA: String = "haha"
        }
    }

    /**
     * Renders a drawable image for the "haha" reaction type, and falls back to the default
     * emoji rendering for all other types.
     */
    class ImageReactionComponentFactory : ChatComponentFactory {

        @Composable
        override fun ReactionIcon(
            type: String,
            emoji: String?,
            size: ReactionIconSize,
            modifier: Modifier,
        ) {
            if (type == CustomReactionResolver.HAHA) {
                Image(
                    painter = painterResource(R.drawable.stream_compose_ic_reaction_lol),
                    contentDescription = type,
                    modifier = modifier,
                )
            } else {
                super.ReactionIcon(type, emoji, size, modifier)
            }
        }
    }
}
