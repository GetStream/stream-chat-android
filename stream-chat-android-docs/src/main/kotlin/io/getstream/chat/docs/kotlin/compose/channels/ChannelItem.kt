// ktlint-disable filename

package io.getstream.chat.docs.kotlin.compose.channels

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.compose.ui.channels.list.ChannelItem
import io.getstream.chat.android.compose.ui.channels.list.ChannelList
import io.getstream.chat.android.compose.ui.channels.list.SearchResultItem
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.compose.viewmodel.channels.ChannelViewModelFactory
import io.getstream.chat.android.models.User

/**
 * [Usage](https://getstream.io/chat/docs/sdk/android/compose/channel-components/channel-item/#usage)
 */
private object ChannelItemUsageSnippet {

    class ChannelsActivity : AppCompatActivity() {
        val listViewModel: ChannelListViewModel by viewModels { ChannelViewModelFactory() }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                val user by listViewModel.user.collectAsState() // Fetch user

                ChatTheme {
                    ChannelList(
                        channelContent = { channelState -> // Customize the channel items
                            ChannelItem(
                                channelItem = channelState,
                                currentUser = user,
                                onChannelClick = {},
                                onChannelLongClick = {}
                            )
                        },
                        searchResultContent = { searchResultState -> // Customize the search result items
                            SearchResultItem(
                                searchResultItemState = searchResultState,
                                currentUser = user,
                                onSearchResultClick = { },
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

    class ChannelsActivity : AppCompatActivity() {
        val listViewModel: ChannelListViewModel by viewModels { ChannelViewModelFactory() }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                val user by listViewModel.user.collectAsState() // Fetch user

                ChatTheme {
                    ChannelList(
                        channelContent = { channelState -> // Customize the channel items
                            ChannelItem(
                                channelItem = channelState,
                                currentUser = user,
                                onChannelClick = {
                                    // Start the MessagesScreen
                                },
                                onChannelLongClick = {
                                    listViewModel.selectChannel(it)
                                }
                            )
                        },
                        searchResultContent = { searchResultState -> // Customize the search result items
                            SearchResultItem(
                                searchResultItemState = searchResultState,
                                currentUser = user,
                                onSearchResultClick = { },
                            )
                        },
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
        val listViewModel: ChannelListViewModel by viewModels { ChannelViewModelFactory() }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                val user by listViewModel.user.collectAsState() // Fetch user

                ChatTheme {
                    ChannelList(
                        channelContent = { channelState -> // Customize the channel items
                            CustomChannelListItem(channelItem = channelState, user = user)
                        }
                    )
                }
            }
        }

        @Composable
        fun CustomChannelListItem(channelItem: ItemState.ChannelItemState, user: User?) {
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
