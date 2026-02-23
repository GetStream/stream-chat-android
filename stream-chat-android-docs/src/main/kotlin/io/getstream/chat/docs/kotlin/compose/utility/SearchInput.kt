// ktlint-disable filename

package io.getstream.chat.docs.kotlin.compose.utility

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.state.channels.list.SearchQuery
import io.getstream.chat.android.compose.ui.components.SearchInput
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListViewModel

/**
 * [Usage](https://getstream.io/chat/docs/sdk/android/compose/utility-components/search-input/#usage)
 */
private object SearchInputUsageSnippet {

    @Composable
    fun MySearchInput() {
        // Remember search query for recomposition
        var searchQuery by rememberSaveable { mutableStateOf("") }

        SearchInput(
            modifier = Modifier
                .background(color = ChatTheme.colors.appBackground)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth(),
            query = searchQuery,
            onSearchStarted = {},
            onValueChange = {
                searchQuery = it
            },
        )
    }
}

/**
 * [Usage](https://getstream.io/chat/docs/sdk/android/compose/utility-components/search-input/#handling-actions)
 */
private object SearchInputHandlingActionsSnippet {

    lateinit var channelListViewModel: ChannelListViewModel

    @Composable
    fun MySearchInput() {
        var searchQuery by rememberSaveable { mutableStateOf("") }

        SearchInput(
            query = searchQuery,
            onValueChange = {
                searchQuery = it

                // Use ChannelListViewModel to search for channels
                channelListViewModel.setSearchQuery(SearchQuery.Channels(it))
            }
        )
    }
}

/**
 * [Customization](https://getstream.io/chat/docs/sdk/android/compose/utility-components/search-input/#customization)
 */
private object SearchInputCustomizationSnippet {

    lateinit var channelListViewModel: ChannelListViewModel

    @Composable
    fun MySearchInput() {
        var searchQuery by rememberSaveable { mutableStateOf("") }

        SearchInput(
            modifier = Modifier
                .background(color = ChatTheme.colors.appBackground)
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .fillMaxWidth(),
            query = searchQuery,
            onValueChange = {
                searchQuery = it

                // Use ChannelListViewModel to search for channels
                channelListViewModel.setSearchQuery(SearchQuery.Channels(it))
            },
            leadingIcon = {
                // Remove the leading icon
                Spacer(Modifier.width(16.dp))
            },
            label = {
                // Customize the hint
                Text(
                    text = "Search channels",
                    style = ChatTheme.typography.body,
                    color = ChatTheme.colors.textSecondary,
                )
            }
        )
    }
}
