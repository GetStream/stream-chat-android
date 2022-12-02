// ktlint-disable filename

package io.getstream.chat.docs.kotlin.compose.messages

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.compose.ui.messages.MessagesScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory

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
                    MessagesScreen(
                        viewModelFactory = MessagesViewModelFactory(
                            context = this,
                            channelId = channelId
                        )
                    )
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
                        viewModelFactory = MessagesViewModelFactory(
                            context = this,
                            channelId = channelId
                        ),
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
                        viewModelFactory = MessagesViewModelFactory(
                            context = this,
                            channelId = channelId,
                            messageLimit = 30,
                            enforceUniqueReactions = true,
                            showSystemMessages = true),
                        showHeader = true,
                    )
                }
            }
        }
    }
}
