// ktlint-disable filename

package io.getstream.chat.docs.kotlin.compose.channels

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.ui.channels.ChannelsScreen
import io.getstream.chat.android.compose.ui.channels.SearchMode
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.compose.viewmodel.channels.ChannelViewModelFactory
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.querysort.QuerySortByField

/**
 * [Usage](https://getstream.io/chat/docs/sdk/android/compose/channel-components/channels-screen/#usage)
 */
private object ChannelsScreenUsageSnippet {

    class ChannelsActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                ChatTheme {
                    ChannelsScreen()
                }
            }
        }
    }
}

/**
 * [Handling Actions](https://getstream.io/chat/docs/sdk/android/compose/channel-components/channels-screen/#handling-actions)
 */
private object ChannelsScreenHandlingActionsSnippet {

    class ChannelsActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                ChatTheme {
                    ChannelsScreen(
                        onItemClick = {
                            // Open messages screen
                        },
                        onHeaderActionClick = {
                            // Handle the header click action
                        },
                        onHeaderAvatarClick = {
                            // Handle the header avatar clicks
                        },
                        onViewChannelInfoAction = {
                            // Show UI to view more channel info
                        },
                        onBackPressed = { finish() },
                    )
                }
            }
        }
    }
}

/**
 * [Customization](https://getstream.io/chat/docs/sdk/android/compose/channel-components/channels-screen/#customization)
 */
private object ChannelsScreenCustomizationSnippet {

    class ChannelsActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                ChatTheme {
                    ChannelsScreen(
                        viewModelFactory = ChannelViewModelFactory(
                            filters = Filters.and(
                                Filters.eq("type", "messaging"),
                                Filters.`in`("members", listOf(ChatClient.instance().getCurrentUser()?.id ?: ""))
                            ),
                            querySort = QuerySortByField.descByName("last_updated"),
                            channelLimit = 30,
                            memberLimit = 30,
                            messageLimit = 1
                        ),
                        title = "Stream Chat",
                        isShowingHeader = true,
                        searchMode = SearchMode.Channels,
                    )
                }
            }
        }
    }
}

/**
 * [Customization](https://getstream.io/chat/docs/sdk/android/compose/channel-components/channels-screen/#overriding-the-viewmodels)
 */
private object ChannelsScreenOverridingViewModelsSnippet {

    class ChannelsActivity : AppCompatActivity() {

        private val factory by lazy {
            ChannelViewModelFactory(
                chatClient = ChatClient.instance(),
                querySort = QuerySortByField.descByName("last_updated"),
                filters = null
            )
        }

        private val listViewModel: ChannelListViewModel by viewModels { factory }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                ChannelsScreen(
                    viewModelFactory = factory
                )
            }
        }
    }
}