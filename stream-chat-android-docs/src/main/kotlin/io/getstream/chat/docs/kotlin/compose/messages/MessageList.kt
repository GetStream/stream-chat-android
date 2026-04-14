// ktlint-disable filename

package io.getstream.chat.docs.kotlin.compose.messages

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.components.messages.MessageBubble
import io.getstream.chat.android.compose.ui.messages.list.MessageList
import io.getstream.chat.android.compose.ui.theme.ChatComponentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageItemParams
import io.getstream.chat.android.compose.ui.util.rememberMessageListState
import io.getstream.chat.android.compose.viewmodel.messages.ChannelViewModelFactory
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState

/**
 * [Usage](https://getstream.io/chat/docs/sdk/android/compose/message-components/message-list/#usage)
 */
private object MessageListUsageSnippet {

    class MyActivity : AppCompatActivity() {
        val factory by lazy {
            ChannelViewModelFactory(
                context = this,
                channelId = "messaging:123",
            )
        }

        val listViewModel by viewModels<MessageListViewModel>(factoryProducer = { factory })

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                ChatTheme {
                    Column(
                        Modifier.fillMaxSize()
                    ) {
                        // ChannelHeader(...)

                        MessageList(
                            viewModel = listViewModel,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Rest of your UI
                    }
                }
            }
        }
    }
}

/**
 * [Handling Actions](https://getstream.io/chat/docs/sdk/android/compose/message-components/message-list/#handling-actions)
 */
private object MessageListHandlingActionsSnippet {

    class MyActivity : AppCompatActivity() {
        val factory by lazy {
            ChannelViewModelFactory(
                context = this,
                channelId = "messaging:123",
            )
        }

        val listViewModel by viewModels<MessageListViewModel>(factoryProducer = { factory })

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                ChatTheme {
                    MessageList(
                        viewModel = listViewModel,
                        // Actions
                        onThreadClick = { message -> },
                        onLongItemClick = { message -> },
                        onMessagesPageStartReached = { },
                        onLastVisibleMessageChanged = { message -> },
                        onScrollToBottom = { },
                        onMediaGalleryPreviewResult = { mediaGalleryPreviewResult -> },
                    )
                }
            }
        }
    }
}

/**
 * [Controlling Scroll State](https://getstream.io/chat/docs/sdk/android/compose/message-components/message-list/#controlling-the-scroll-state)
 */
private object MessageListControllingScrollStateSnippet {

    class MyActivity : AppCompatActivity() {
        val factory by lazy {
            ChannelViewModelFactory(
                context = this,
                channelId = "messaging:123",
            )
        }

        val listViewModel by viewModels<MessageListViewModel>(factoryProducer = { factory })

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                ChatTheme {
                    val state = listViewModel.currentMessagesState
                    val myListState = rememberMessageListState(parentMessageId = state.value.parentMessageId)

                    MessageList(
                        viewModel = listViewModel,
                        messagesLazyListState = myListState,
                    )
                }
            }
        }
    }
}

/**
 * [Customization](https://getstream.io/chat/docs/sdk/android/compose/message-components/message-list/#customization)
 */
private object MessageListCustomizationSnippet {

    class MyActivity : AppCompatActivity() {
        val factory by lazy {
            ChannelViewModelFactory(
                context = this,
                channelId = "messaging:123",
            )
        }

        val listViewModel by viewModels<MessageListViewModel>(factoryProducer = { factory })

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                // Override the component factory to customize how message items are rendered.
                ChatTheme(componentFactory = CustomMessageItemFactory) {
                    MessageList(
                        viewModel = listViewModel,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }

    // Customize the message item to show an avatar, username, and a message bubble.
    private object CustomMessageItemFactory : ChatComponentFactory {
        @Composable
        override fun LazyItemScope.MessageItem(params: MessageItemParams) {
            val messageListItem = params.messageListItem
            if (messageListItem is MessageItemState) {
                val message = messageListItem.message

                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .widthIn(max = 300.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserAvatar(
                            modifier = Modifier.size(36.dp),
                            user = message.user,
                        )

                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            text = message.user.name,
                            style = ChatTheme.typography.bodyEmphasis,
                            fontSize = 14.sp,
                            color = ChatTheme.colors.textPrimary,
                        )
                    }

                    MessageBubble(
                        color = ChatTheme.colors.backgroundCoreElevation1,
                        modifier = Modifier.padding(top = 4.dp),
                        shape = RoundedCornerShape(
                            topEnd = 16.dp,
                            topStart = 0.dp,
                            bottomEnd = 16.dp,
                            bottomStart = 16.dp,
                        ),
                        content = {
                            Text(
                                modifier = Modifier.padding(8.dp),
                                text = message.text,
                                color = ChatTheme.colors.textPrimary,
                            )
                        },
                    )
                }
            }
        }
    }
}

