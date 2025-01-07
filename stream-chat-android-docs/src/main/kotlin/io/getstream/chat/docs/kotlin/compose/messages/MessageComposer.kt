// ktlint-disable filename

package io.getstream.chat.docs.kotlin.compose.messages

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.components.composer.MessageInput
import io.getstream.chat.android.compose.ui.messages.composer.MessageComposer
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsPickerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.docs.R

/**
 * [Usage](https://getstream.io/chat/docs/sdk/android/compose/message-components/message-composer/#usage)
 */
private object MessageComposerUsageSnippet {

    class MyActivity : AppCompatActivity() {

        val factory by lazy {
            MessagesViewModelFactory(
                context = this,
                channelId = "messaging:123",
            )
        }

        val listViewModel by viewModels<MessageListViewModel>(factoryProducer = { factory })
        val attachmentsPickerViewModel by viewModels<AttachmentsPickerViewModel>(factoryProducer = { factory })
        val composerViewModel by viewModels<MessageComposerViewModel>(factoryProducer = { factory })

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                ChatTheme {
                    MyCustomUi()
                }
            }
        }

        @Composable
        fun MyCustomUi() {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = { // 1 - Add the composer as a bottom bar
                    MessageComposer(
                        modifier = Modifier // 2 - customize the component
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        viewModel = composerViewModel, // 3 - provide ViewModel
                        // 4 - customize actions
                        onAttachmentsClick = { attachmentsPickerViewModel.changeAttachmentState(true) },
                        onCancelAction = {
                            listViewModel.dismissAllMessageActions()
                            composerViewModel.dismissMessageActions()
                        }
                    )
                }
            ) {
                // 5 - the rest of your UI
                // ...
            }
        }
    }
}

/**
 * [Handling Actions](https://getstream.io/chat/docs/sdk/android/compose/message-components/message-composer/#handling-actions)
 */
private object MessageComposerHandlingActionsSnippet {

    class MyActivity : AppCompatActivity() {

        val factory by lazy {
            MessagesViewModelFactory(
                context = this,
                channelId = "messaging:123",
            )
        }

        val viewModel by viewModels<MessageComposerViewModel>(factoryProducer = { factory })

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                ChatTheme {
                    MessageComposer(
                        viewModel = viewModel,
                        onSendMessage = { viewModel.sendMessage(it) },
                        onAttachmentsClick = {},
                        onCommandsClick = {},
                        onValueChange = { viewModel.setMessageInput(it) },
                        onAttachmentRemoved = { viewModel.removeSelectedAttachment(it) },
                        onCancelAction = { viewModel.dismissMessageActions() },
                        onMentionSelected = { viewModel.selectMention(it) },
                        onCommandSelected = { viewModel.selectCommand(it) },
                        onAlsoSendToChannelSelected = { viewModel.setAlsoSendToChannel(it) },
                    )
                }
            }
        }
    }
}

/**
 * [Handling Typing Updates](https://getstream.io/chat/docs/sdk/android/compose/message-components/message-composer/#handling-actions)
 */
private object HandlingTypingUpdatesSnippet {

    class MyActivity : AppCompatActivity() {
        val factory by lazy {
            MessagesViewModelFactory(
                context = this,
                channelId = "messaging:123",
            )
        }

        val composerViewModel = factory.create(MessageComposerViewModel::class.java)

        override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
            super.onCreate(savedInstanceState, persistentState)

            // This needs to be commented out or the docs module build will fail
            /*
            composerViewModel.setTypingUpdatesBuffer(
                // Your custom implementation of TypingUpdatesBuffer
            )
            */
        }
    }
}

/**
 * [Customization](https://getstream.io/chat/docs/sdk/android/compose/message-components/message-composer/#customization)
 */
private object MessageComposerCustomizationSnippet {

    class MyActivity : AppCompatActivity() {

        val factory by lazy {
            MessagesViewModelFactory(
                context = this,
                channelId = "messaging:123",
            )
        }

        val composerViewModel by viewModels<MessageComposerViewModel>(factoryProducer = { factory })

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                ChatTheme {
                    MyCustomComposer()
                }
            }
        }

        @Composable
        fun MyCustomComposer() {
            MessageComposer(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                viewModel = composerViewModel,
                integrations = {},
                input = { inputState ->
                    MessageInput(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(7f)
                            .padding(start = 8.dp),
                        messageComposerState = inputState,
                        onValueChange = { composerViewModel.setMessageInput(it) },
                        onAttachmentRemoved = { composerViewModel.removeSelectedAttachment(it) },
                        label = { // create a custom label with an icon
                            Row(
                                Modifier.wrapContentWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null
                                )

                                Text(
                                    modifier = Modifier.padding(start = 4.dp),
                                    text = "Type something",
                                    color = ChatTheme.colors.textLowEmphasis
                                )
                            }
                        },
                        innerTrailingContent = { // add a send button inside the input
                            Icon(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple()
                                    ) {
                                        val state = composerViewModel.messageComposerState.value

                                        composerViewModel.sendMessage(
                                            composerViewModel.buildNewMessage(
                                                state.inputValue,
                                                state.attachments
                                            )
                                        )
                                    },
                                painter = painterResource(id = R.drawable.stream_compose_ic_send),
                                tint = ChatTheme.colors.primaryAccent,
                                contentDescription = null
                            )
                        },
                    )
                },
                trailingContent = { Spacer(modifier = Modifier.size(8.dp)) } // remove the outer send button
            )
        }
    }
}
