// ktlint-disable filename

package io.getstream.chat.docs.kotlin.compose.messages

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.components.messages.MessageBubble
import io.getstream.chat.android.compose.ui.messages.list.MessageList
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.rememberMessageListState
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState

/**
 * [Usage](https://getstream.io/chat/docs/sdk/android/compose/message-components/message-list/#usage)
 */
private object MessageListUsageSnippet {

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
                    Column(
                        Modifier.fillMaxSize()
                    ) {
                        // MessageListHeader(...)

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
                    MessageList(
                        viewModel = listViewModel,
                        // Actions
                        onThreadClick = { message -> },
                        onLongItemClick = { message -> },
                        onReactionsClick = { message -> },
                        onMessagesPageStartReached = { },
                        onLastVisibleMessageChanged = { message -> },
                        onScrollToBottom = { },
                        onGiphyActionClick = { giphyAction -> },
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
                    MessageList(
                        viewModel = listViewModel,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(ChatTheme.colors.appBackground),
                        itemContent = { item ->
                            if (item is MessageItemState) { // we check against other subclasses of 'MessageListItemState'
                                val message = item.message

                                Column(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .widthIn(max = 300.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        UserAvatar(
                                            modifier = Modifier.size(36.dp),
                                            user = message.user,
                                        )

                                        Text(
                                            modifier = Modifier.padding(start = 8.dp),
                                            text = message.user.name,
                                            style = ChatTheme.typography.bodyBold,
                                            fontSize = 14.sp,
                                            color = ChatTheme.colors.textPrimary
                                        )
                                    }

                                    MessageBubble(
                                        color = ChatTheme.colors.backgroundElevationElevation1,
                                        modifier = Modifier.padding(top = 4.dp),
                                        shape = RoundedCornerShape(
                                            topEnd = 16.dp,
                                            topStart = 0.dp,
                                            bottomEnd = 16.dp,
                                            bottomStart = 16.dp
                                        ),
                                        content = {
                                            Text(
                                                modifier = Modifier.padding(8.dp),
                                                text = message.text,
                                                color = ChatTheme.colors.textPrimary
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
