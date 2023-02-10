// ktlint-disable filename

package io.getstream.chat.docs.kotlin.compose.guides

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.getstream.sdk.chat.audio.recording.DefaultStreamMediaRecorder
import com.getstream.sdk.chat.audio.recording.StreamMediaRecorder
import io.getstream.chat.android.compose.state.messages.attachments.StatefulStreamMediaRecorder
import io.getstream.chat.android.compose.ui.messages.MessagesScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.ReactionIcon
import io.getstream.chat.android.compose.ui.util.ReactionIconFactory
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.docs.R

/**
 * [Providing Custom Reactions](https://getstream.io/chat/docs/sdk/android/compose/guides/providing-custom-reactions/)
 */
private object ProvidingCustomReactionsSnippet {

    //TODO add this and related entries to docs when documentation effort occurs
    private val streamMediaRecorder: StreamMediaRecorder = DefaultStreamMediaRecorder()
    private val statefulStreamMediaRecorder = StatefulStreamMediaRecorder(streamMediaRecorder)

    class MessagesActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            val channelId = requireNotNull(intent.getStringExtra(KEY_CHANNEL_ID))

            setContent {
                // Provide a factory with custom reactions
                ChatTheme(reactionIconFactory = CustomReactionIconFactory()) {
                    MessagesScreen(
                        viewModelFactory = MessagesViewModelFactory(
                            context = this,
                            channelId = channelId,
                        ),
                        onBackPressed = { finish() },
                        onHeaderTitleClick = {},
                        //TODO add this and related entries to docs when documentation effort occurs
                        statefulStreamMediaRecorder = statefulStreamMediaRecorder,
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
                    selectedPainter = painterResource(R.drawable.ic_thumb_up_selected)
                )
                THUMBS_DOWN -> ReactionIcon(
                    painter = painterResource(R.drawable.ic_thumb_down),
                    selectedPainter = painterResource(R.drawable.ic_thumb_down_selected)
                )
                MOOD_GOOD -> ReactionIcon(
                    painter = painterResource(R.drawable.ic_mood_good),
                    selectedPainter = painterResource(R.drawable.ic_mood_good_selected)
                )
                MOOD_BAD -> ReactionIcon(
                    painter = painterResource(R.drawable.ic_mood_bad),
                    selectedPainter = painterResource(R.drawable.ic_mood_bad_selected)
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
                MOOD_BAD
            )
        }
    }
}