// ktlint-disable filename

package io.getstream.chat.docs.kotlin.compose.channels

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.compose.ui.channels.list.ChannelList
import io.getstream.chat.android.compose.ui.components.avatar.ChannelAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.compose.viewmodel.channels.ChannelViewModelFactory

/**
 * [Usage](https://getstream.io/chat/docs/sdk/android/compose/channel-components/channel-list/#usage)
 */
private object ChannelListUsageSnippet {

    class MyActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                ChatTheme {
                    ChannelList(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

/**
 * [Handling Actions](https://getstream.io/chat/docs/sdk/android/compose/channel-components/channel-list/#handling-actions)
 */
private object ChannelListHandlingActionsSnippet1 {

    class MyActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                ChatTheme {
                    // Custom state holder
                    var selectedChannel by remember { mutableStateOf<Channel?>(null) }

                    Box(modifier = Modifier.fillMaxSize()) {
                        ChannelList(
                            modifier = Modifier.fillMaxSize(),
                            onChannelLongClick = { // Custom long tap handler
                                selectedChannel = it
                            },
                            onChannelClick = {
                                // Start the MessagesScreen
                            },
                        )

                        if (selectedChannel != null) {
                            // Show custom UI
                        }
                    }
                }
            }
        }
    }
}

/**
 * [Handling Actions](https://getstream.io/chat/docs/sdk/android/compose/channel-components/channel-list/#handling-actions)
 */
private object ChannelListHandlingActionsSnippet2 {

    class MyActivity : AppCompatActivity() {
        val factory by lazy {
            ChannelViewModelFactory(
                ChatClient.instance(),
                QuerySortByField.descByName("last_updated"),
                Filters.and(
                    Filters.eq("type", "messaging"),
                    Filters.`in`("members", listOf(ChatClient.instance().getCurrentUser()?.id ?: ""))
                )
            )
        }

        val listViewModel: ChannelListViewModel by viewModels { factory }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                ChatTheme {
                    Box(modifier = Modifier.fillMaxSize()) {
                        ChannelList(
                            modifier = Modifier.fillMaxSize(),
                            viewModel = listViewModel, // Passing in our ViewModel
                            onChannelClick = {
                                // Start the MessagesScreen
                            }
                        )

                        if (listViewModel.selectedChannel != null) {
                            // Show custom UI
                        }
                    }
                }
            }
        }
    }
}

/**
 * [Controlling Scroll State](https://getstream.io/chat/docs/sdk/android/compose/channel-components/channel-list/#controlling-the-scroll-state)
 */
private object ChannelListControllingScrollStateSnippet {

    class MyActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                ChatTheme {
                    val myListState = rememberLazyListState()
                    ChannelList(
                        // State
                        lazyListState = myListState
                        // Actions & Content Slots
                    )
                }
            }
        }
    }
}

/**
 * [Customization](https://getstream.io/chat/docs/sdk/android/compose/channel-components/channel-list/#customization)
 */
private object ChannelListCustomizationSnippet {

    class MyActivity : AppCompatActivity() {
        val factory by lazy {
            ChannelViewModelFactory(
                ChatClient.instance(),
                QuerySortByField.descByName("last_updated"),
                Filters.and(
                    Filters.eq("type", "messaging"),
                    Filters.`in`("members", listOf(ChatClient.instance().getCurrentUser()?.id ?: ""))
                )
            )
        }

        val listViewModel: ChannelListViewModel by viewModels { factory }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                ChatTheme {
                    CustomChannelListItem()
                }
            }
        }

        @Composable
        fun CustomChannelListItem() {
            val user by listViewModel.user.collectAsState() // Fetch user

            ChannelList(
                // Set up state
                itemContent = { // Customize the channel items
                    Card(
                        modifier = Modifier
                            .padding(2.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ChannelAvatar(
                                modifier = Modifier.size(40.dp),
                                channel = it.channel,
                                currentUser = user
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = ChatTheme.channelNameFormatter.formatChannelName(it.channel, user),
                                style = ChatTheme.typography.bodyBold,
                                maxLines = 1,
                            )
                        }
                    }
                }
            )
        }
    }
}
