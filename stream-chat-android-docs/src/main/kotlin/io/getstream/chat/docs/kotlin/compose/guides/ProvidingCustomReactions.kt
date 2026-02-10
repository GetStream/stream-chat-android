// ktlint-disable filename

package io.getstream.chat.docs.kotlin.compose.guides

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.compose.ui.messages.MessagesScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory

// TODO [G.]
/**
 * [Providing Custom Reactions](https://getstream.io/chat/docs/sdk/android/compose/guides/providing-custom-reactions/)
 */
private object ProvidingCustomReactionsSnippet {

    class MessagesActivity : AppCompatActivity() {

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
}
