// ktlint-disable filename

package io.getstream.chat.docs.kotlin.compose.messages

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.compose.ui.messages.MessagesScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * [Usage](https://getstream.io/chat/docs/sdk/android/compose/message-components/messages-screen/#usage)
 */
private object MessagesScreenUsageSnippet {

    class MyActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            // The ID of the channel you've opened
            val channelId = "messaging:123"

            setContent {
                ChatTheme {
                    MessagesScreen(channelId = channelId)
                }
            }
        }
    }
}

/**
 * [Handling Actions](https://getstream.io/chat/docs/sdk/android/compose/message-components/messages-screen/#handling-actions)
 */
private object MessagesScreenHandlingActionsSnippet {

    class MyActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            // The ID of the channel you've opened
            val channelId = "messaging:123"

            setContent {
                ChatTheme {
                    MessagesScreen(
                        channelId = channelId,
                        onBackPressed = { finish() }, // Navigation handler
                        onHeaderActionClick = { channel ->
                            // Show channel info
                        },
                    )
                }
            }
        }
    }
}

/**
 * [Customization](https://getstream.io/chat/docs/sdk/android/compose/message-components/messages-screen/#customization)
 */
private object MessagesScreenCustomizationSnippet {

    class MyActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            // The ID of the channel you've opened
            val channelId = "messaging:123"

            setContent {
                ChatTheme {
                    MessagesScreen(
                        channelId = channelId,
                        messageLimit = 30,
                        showHeader = true,
                        enforceUniqueReactions = true,
                        showDateSeparators = true,
                        showSystemMessages = true,
                    )
                }
            }
        }
    }
}
