// ktlint-disable filename

package io.getstream.chat.docs.kotlin.compose.messages

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.getstream.chat.android.compose.ui.components.reactionpicker.ReactionsPicker
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.ui.common.state.messages.list.SelectedMessageReactionsPickerState

/**
 * [Usage](https://getstream.io/chat/docs/sdk/android/compose/message-components/reactions-picker/#usage)
 */
private object ReactionsPickerUsageSnippet {

    class MyActivity : AppCompatActivity() {
        val factory by lazy {
            MessagesViewModelFactory(
                context = this,
                channelId = "messaging:123",
            )
        }

        val listViewModel by viewModels<MessageListViewModel>(factoryProducer = { factory })

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                ChatTheme {
                    val currentMessagesState by listViewModel.currentMessagesState
                    val selectedMessageState = currentMessagesState.selectedMessageState

                    Box(modifier = Modifier.fillMaxSize()) {

                        // The rest of your UI
                        if (selectedMessageState != null) {
                            val selectedMessage = selectedMessageState.message
                            if (selectedMessageState is SelectedMessageReactionsPickerState) {
                                ReactionsPicker(
                                    message = selectedMessage,
                                    onMessageAction = { action ->
                                        // Handle message action
                                    },
                                    onDismiss = {
                                        // Handle on dismiss
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * [Handling Actions](https://getstream.io/chat/docs/sdk/android/compose/message-components/reactions-picker/#handling-actions)
 */
private object ReactionsPickerHandlingActionsSnippet {

    class MyActivity : AppCompatActivity() {
        val factory by lazy {
            MessagesViewModelFactory(
                context = this,
                channelId = "messaging:123",
            )
        }

        val listViewModel by viewModels<MessageListViewModel>(factoryProducer = { factory })
        val composerViewModel by viewModels<MessageComposerViewModel>(factoryProducer = { factory })

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                ChatTheme {
                    val currentMessagesState by listViewModel.currentMessagesState
                    val selectedMessageState = currentMessagesState.selectedMessageState

                    Box(modifier = Modifier.fillMaxSize()) {

                        // The rest of your UI

                        if (selectedMessageState != null) {
                            val selectedMessage = selectedMessageState.message
                            if (selectedMessageState is SelectedMessageReactionsPickerState) {
                                ReactionsPicker(
                                    message = selectedMessage,
                                    onMessageAction = { action ->
                                        composerViewModel.performMessageAction(action)
                                        listViewModel.performMessageAction(action)
                                    },
                                    onDismiss = { listViewModel.removeOverlay() },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * [Customization](https://getstream.io/chat/docs/sdk/android/compose/message-components/reactions-picker/#customization)
 */
private object ReactionsPickerCustomizationSnippet {

    class MyActivity : AppCompatActivity() {
        val factory by lazy {
            MessagesViewModelFactory(
                context = this,
                channelId = "messaging:123",
            )
        }

        val listViewModel by viewModels<MessageListViewModel>(factoryProducer = { factory })
        val composerViewModel by viewModels<MessageComposerViewModel>(factoryProducer = { factory })

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                ChatTheme {
                    val currentMessagesState by listViewModel.currentMessagesState
                    val selectedMessageState = currentMessagesState.selectedMessageState

                    Box(modifier = Modifier.fillMaxSize()) {

                        // The rest of your UI

                        if (selectedMessageState != null) {
                            val selectedMessage = selectedMessageState.message
                            if (selectedMessageState is SelectedMessageReactionsPickerState) {

                                ReactionsPicker(
                                    message = selectedMessage,
                                    onMessageAction = { action ->
                                        composerViewModel.performMessageAction(action)
                                        listViewModel.performMessageAction(action)
                                    },
                                    onDismiss = { listViewModel.removeOverlay() },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
