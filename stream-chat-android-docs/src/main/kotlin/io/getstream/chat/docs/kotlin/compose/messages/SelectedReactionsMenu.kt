// ktlint-disable filename

package io.getstream.chat.docs.kotlin.compose.messages

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.components.selectedmessage.SelectedReactionsMenu
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.ui.common.state.messages.list.SelectedMessageReactionsState

/**
 * [Usage](https://getstream.io/chat/docs/sdk/android/compose/message-components/selected-reactions-menu/#usage)
 */
private object SelectedReactionsMenuUsageSnippet {

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
                    val selectedMessageState = listViewModel.currentMessagesState.selectedMessageState
                    val user by listViewModel.user.collectAsState()

                    Box(modifier = Modifier.fillMaxSize()) {

                        // The rest of your UI

                        if (selectedMessageState is SelectedMessageReactionsState) {
                            val selectedMessage = selectedMessageState.message
                            SelectedReactionsMenu(
                                modifier = Modifier.align(Alignment.BottomCenter),
                                // The currently logged-in user
                                currentUser = user,
                                // The capabilities the user has in a given channel
                                ownCapabilities = selectedMessageState.ownCapabilities,
                                // The message whose reactions you selected
                                message = selectedMessage,
                                onMessageAction = { action ->
                                    // Handle message action
                                },
                                onShowMoreReactionsSelected = {
                                    // Handle show more reactions button click
                                },
                                onDismiss = {
                                    // Handle dismiss
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * [Handling Actions](https://getstream.io/chat/docs/sdk/android/compose/message-components/selected-reactions-menu/#handling-actions)
 */
private object SelectedReactionsMenuHandlingActionsSnippet {

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
                    val selectedMessageState = listViewModel.currentMessagesState.selectedMessageState
                    val user by listViewModel.user.collectAsState()

                    Box(modifier = Modifier.fillMaxSize()) {

                        // The rest of your UI

                        if (selectedMessageState is SelectedMessageReactionsState) {
                            val selectedMessage = selectedMessageState.message
                            SelectedReactionsMenu(
                                modifier = Modifier.align(Alignment.BottomCenter),
                                // The currently logged-in user
                                currentUser = user,
                                // The capabilities the user has in a given channel
                                ownCapabilities = selectedMessageState.ownCapabilities,
                                // The message whose reactions you selected
                                message = selectedMessage,
                                onMessageAction = { action ->
                                    composerViewModel.performMessageAction(action)
                                    listViewModel.performMessageAction(action)
                                },
                                onShowMoreReactionsSelected = {
                                    listViewModel.selectExtendedReactions(selectedMessage)
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

/**
 * [Customization](https://getstream.io/chat/docs/sdk/android/compose/message-components/selected-reactions-menu/#customization)
 */
private object SelectedReactionsMenuCustomizationSnippet {

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
                    val selectedMessageState = listViewModel.currentMessagesState.selectedMessageState
                    val user by listViewModel.user.collectAsState()

                    Box(modifier = Modifier.fillMaxSize()) {

                        // The rest of your UI

                        if (selectedMessageState is SelectedMessageReactionsState) {
                            val selectedMessage = selectedMessageState.message
                            SelectedReactionsMenu(
                                // Use a Modifier to customize the appearance
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(horizontal = 20.dp)
                                    .wrapContentSize(),
                                // The currently logged-in user
                                currentUser = user,
                                // The capabilities the user has in a given channel
                                ownCapabilities = selectedMessageState.ownCapabilities,
                                // The message whose reactions you selected
                                message = selectedMessage,
                                // Assign a different shape to the Composable element
                                shape = ChatTheme.shapes.attachment,
                                onMessageAction = { action ->
                                    // Handle message action
                                },
                                onShowMoreReactionsSelected = {
                                    // Handle show more reactions button click
                                },
                                onDismiss = {
                                    // Handle dismiss
                                },
                                // Custom header content
                                headerContent = {
                                    Text(
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .background(
                                                shape = ChatTheme.shapes.avatar,
                                                color = ChatTheme.colors.infoAccent
                                            )
                                            .padding(horizontal = 8.dp),
                                        style = ChatTheme.typography.body,
                                        color = ChatTheme.colors.textHighEmphasis,
                                        text = "User Reactions"
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
