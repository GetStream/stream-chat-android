// ktlint-disable filename

package io.getstream.chat.docs.kotlin.compose.channels

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.channels.list.ChannelItem
import io.getstream.chat.android.compose.ui.channels.list.ChannelList
import io.getstream.chat.android.compose.ui.theme.ChannelListItemContentParams
import io.getstream.chat.android.compose.ui.theme.ChatComponentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListViewModelFactory

/**
 * [Usage](https://getstream.io/chat/docs/sdk/android/compose/channel-components/channel-item/#usage)
 */
private object ChannelItemUsageSnippet {

    class ChannelsActivity : AppCompatActivity() {
        val listViewModel: ChannelListViewModel by viewModels { ChannelListViewModelFactory() }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                // Override the component factory to control how each channel is rendered.
                ChatTheme(componentFactory = CustomChannelItemFactory) {
                    ChannelList(viewModel = listViewModel)
                }
            }
        }
    }

    private object CustomChannelItemFactory : ChatComponentFactory {
        @Composable
        override fun LazyItemScope.ChannelListItemContent(params: ChannelListItemContentParams) {
            // Customize the channel item
            ChannelItem(
                channelItem = params.channelItem,
                currentUser = params.currentUser,
                onChannelClick = params.onChannelClick,
                onChannelLongClick = params.onChannelLongClick,
            )
        }
    }
}

/**
 * [Handling Actions](https://getstream.io/chat/docs/sdk/android/compose/channel-components/channel-item/#handling-actions)
 */
private object ChannelItemHandlingActionsSnippet {

    class ChannelsActivity : AppCompatActivity() {
        val listViewModel: ChannelListViewModel by viewModels { ChannelListViewModelFactory() }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                // Override action handlers in the component factory
                ChatTheme(componentFactory = CustomActionsFactory) {
                    // onChannelClick and onChannelLongClick can also be passed directly to ChannelList,
                    // which forwards them to ChannelItem via ChannelListItemContentParams.
                    ChannelList(viewModel = listViewModel)
                }
            }
        }
    }

    // Override ChannelItem action handlers in the factory.
    private object CustomActionsFactory : ChatComponentFactory {
        @Composable
        override fun LazyItemScope.ChannelListItemContent(params: ChannelListItemContentParams) {
            ChannelItem(
                channelItem = params.channelItem,
                currentUser = params.currentUser,
                onChannelClick = {
                    // Start the ChannelScreen
                },
                onChannelLongClick = {
                    // Show channel options
                },
            )
        }
    }
}

/**
 * [Customization](https://getstream.io/chat/docs/sdk/android/compose/channel-components/channel-item/#customization)
 */
private object ChannelItemCustomizationSnippet {

    class MyActivity : AppCompatActivity() {
        val listViewModel: ChannelListViewModel by viewModels { ChannelListViewModelFactory() }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                // Override the component factory to customize how ChannelItem is rendered.
                ChatTheme(componentFactory = CustomChannelItemFactory) {
                    ChannelList(viewModel = listViewModel)
                }
            }
        }
    }

    // Customize the channel item by replacing trailing and center content.
    private object CustomChannelItemFactory : ChatComponentFactory {
        @Composable
        override fun LazyItemScope.ChannelListItemContent(params: ChannelListItemContentParams) {
            ChannelItem(
                channelItem = params.channelItem,
                currentUser = params.currentUser,
                onChannelLongClick = params.onChannelLongClick,
                onChannelClick = params.onChannelClick,
                trailingContent = { // Replace the trailing content with a spacer
                    Spacer(modifier = Modifier.width(8.dp))
                },
                centerContent = { // Replace the details content with a simple Text
                    Text(
                        text = ChatTheme.channelNameFormatter.formatChannelName(it.channel, params.currentUser),
                        style = ChatTheme.typography.bodyEmphasis,
                        color = ChatTheme.colors.textPrimary,
                    )
                },
            )
        }
    }
}
