// ktlint-disable filename

package io.getstream.chat.docs.kotlin.compose.messages

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.compose.ui.messages.MessagesScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsPickerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory

/**
 * [Usage](https://getstream.io/chat/docs/sdk/android/compose/message-components/messages-screen/#usage)
 */
private object MessagesScreenUsageSnippet {

    class MyActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            // Load the ID of the channel you've opened
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
            // Load the ID of the channel you've opened
            val channelId = "messaging:123"

            setContent {
                ChatTheme {
                    MessagesScreen(
                        viewModelFactory = MessagesViewModelFactory(
                            context = this,
                            channelId = channelId
                        ),
                        onBackPressed = { finish() }, // Navigation handler
                        onHeaderTitleClick = { channel ->
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
            // Load the ID of the channel you've opened
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

/**
 * [Overriding the ViewModels](https://getstream.io/chat/docs/sdk/android/compose/message-components/messages-screen/#customization)
 */
private object MessageScreenOverridingTheViewModelsSnippet {

    class MessagesActivity : ComponentActivity() {

        // 1
        private val factory by lazy {
            MessagesViewModelFactory(
                context = this,
                channelId = channelId,
                // Customization options
            )
        }

        // 2
        private val listViewModel by viewModels<MessageListViewModel>(factoryProducer = { factory })

        private val attachmentsPickerViewModel by viewModels<AttachmentsPickerViewModel>(factoryProducer = { factory })
        private val composerViewModel by viewModels<MessageComposerViewModel>(factoryProducer = { factory })

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                ChatTheme {
                    MessagesScreen(
                        // 3
                        viewModelFactory = factory,
                        onBackPressed = { finish() },
                    )
                }
            }
        }
    }

    private const val channelId: String =  "message:123"
}
