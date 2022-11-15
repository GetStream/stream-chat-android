// ktlint-disable filename

package io.getstream.chat.docs.kotlin.compose.channels

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.querysort.QuerySortByField
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.state.channels.list.ChannelItemState
import io.getstream.chat.android.compose.ui.channels.list.ChannelItem
import io.getstream.chat.android.compose.ui.channels.list.ChannelList
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.compose.viewmodel.channels.ChannelViewModelFactory

/**
 * [Usage](https://getstream.io/chat/docs/sdk/android/compose/channel-components/channel-item/#usage)
 */
private object ChannelItemUsageSnippet {

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
                val user by listViewModel.user.collectAsState() // Fetch user

                ChatTheme {
                    ChannelList(
                        itemContent = { channelItem -> // Customize the channel items
                            ChannelItem(
                                channelItem = channelItem,
                                currentUser = user,
                                onChannelClick = {},
                                onChannelLongClick = {}
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * [Handling Actions](https://getstream.io/chat/docs/sdk/android/compose/channel-components/channel-item/#handling-actions)
 */
private object ChannelItemHandlingActionsSnippet {

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
                val user by listViewModel.user.collectAsState() // Fetch user

                ChatTheme {
                    ChannelList(
                        itemContent = { channelItem ->
                            ChannelItem(
                                channelItem = channelItem,
                                currentUser = user,
                                onChannelLongClick = {
                                    listViewModel.selectChannel(it)
                                },
                                onChannelClick = {
                                    // Start the MessagesScreen
                                },
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * [Customization](https://getstream.io/chat/docs/sdk/android/compose/channel-components/channel-item/#customization)
 */
private object ChannelItemCustomizationSnippet {

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
                val user by listViewModel.user.collectAsState() // Fetch user

                ChatTheme {
                    ChannelList(
                        itemContent = { channelItem -> // Customize the channel items
                            CustomChannelListItem(channelItem = channelItem, user = user)
                        }
                    )
                }
            }
        }

        @Composable
        fun CustomChannelListItem(channelItem: ChannelItemState, user: User?) {
            ChannelItem(
                channelItem = channelItem,
                currentUser = user,
                onChannelLongClick = {},
                onChannelClick = {},
                trailingContent = { // Replace the trailing content with a spacer
                    Spacer(modifier = Modifier.width(8.dp))
                },
                centerContent = { // Replace the details content with a simple Text
                    Text(
                        text = ChatTheme.channelNameFormatter.formatChannelName(it.channel, user),
                        style = ChatTheme.typography.bodyBold,
                        color = ChatTheme.colors.textHighEmphasis
                    )
                }
            )
        }
    }
}
