// ktlint-disable filename

package io.getstream.chat.docs.kotlin.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.compose.ui.channels.ChannelsScreen
import io.getstream.chat.android.compose.ui.channels.list.ChannelList
import io.getstream.chat.android.compose.ui.components.SearchInput
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.compose.viewmodel.channels.ChannelViewModelFactory

/**
 * [Screen Components Usage](https://getstream.io/chat/docs/sdk/android/compose/component-architecture/#usage)
 */
private object ComponentArchitectureScreenComponentsUsageSnippet {

    class MyActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                ChatTheme { // Theme wrapper
                    ChannelsScreen(
                        title = "App name",
                        onItemClick = {
                            // On item clicked action
                        },
                        onHeaderActionClick = {
                            // Header header action clicks
                        },
                        onHeaderAvatarClick = {
                            // Handle header avatar clicks
                        },
                        onBackPressed = { finish() }
                    )
                }
            }
        }
    }
}

/**
 * [Bound Components Usage](https://getstream.io/chat/docs/sdk/android/compose/component-architecture/#usage-1)
 */
private object ComponentArchitectureBoundComponentUsageSnippet {

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

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            // Your ViewModel instance
            val channelListViewModel: ChannelListViewModel by viewModels { factory }

            setContent {
                ChatTheme { // Theme wrapper
                    ChannelList(
                        modifier = Modifier.fillMaxSize(),
                        viewModel = channelListViewModel,
                        onChannelClick = {
                            // Open the MessagesScreen
                        },
                    )
                }
            }
        }
    }
}

/**
 * [Stateless Components Usage](https://getstream.io/chat/docs/sdk/android/compose/component-architecture/#usage-2)
 */
private object ComponentArchitectureStatelessComponentUsageSnippet {

    class MyActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                var queryState by remember { mutableStateOf("") } // The query state

                SearchInput(
                    query = queryState, // Connect the value to the state holder
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp, horizontal = 24.dp), // Customize the looks
                    onValueChange = {
                        queryState = it // Change the value when typing
                    }
                )
            }
        }
    }
}
