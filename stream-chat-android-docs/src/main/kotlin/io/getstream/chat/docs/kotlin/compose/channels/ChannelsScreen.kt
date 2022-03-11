package io.getstream.chat.docs.kotlin.compose.channels

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.compose.ui.channels.ChannelsScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * [Usage](https://getstream.io/chat/docs/sdk/android/compose/channel-components/channels-screen/#usage)
 */
private object ChannelsScreenUsageSnippet {

    class MyActivity : AppCompatActivity() {

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

    class MyActivity : AppCompatActivity() {

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

    class MyActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                ChatTheme {
                    ChannelsScreen(
                        filters = Filters.and(
                            Filters.eq("type", "messaging"),
                            Filters.`in`("members", listOf(ChatClient.instance().getCurrentUser()?.id ?: ""))
                        ),
                        querySort = QuerySort.desc("last_updated"),
                        title = "Stream Chat",
                        isShowingHeader = true,
                        isShowingSearch = true,
                        channelLimit = 30,
                        memberLimit = 30,
                        messageLimit = 1
                    )
                }
            }
        }
    }
}
