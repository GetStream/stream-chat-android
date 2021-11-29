package io.getstream.chat.android.compose.ui.messages.suggestions.mentions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.ui.messages.suggestions.SuggestionList

/**
 *
 * @param users
 * @param modifier Modifier for styling.
 * @param onUserClick
 * @param itemContent
 */
@ExperimentalFoundationApi
@Composable
public fun MentionSuggestionList(
    users: List<User>,
    modifier: Modifier = Modifier,
    onUserClick: (User) -> Unit = {},
    itemContent: @Composable (User) -> Unit = { user ->
        DefaultMentionSuggestionItem(
            user = user,
            onUserClick = onUserClick,
        )
    },
) {
    SuggestionList(modifier = Modifier) {
        val listState = rememberLazyListState()

        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp),
            modifier = modifier,
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(
                items = users,
                key = User::id
            ) { user ->
                itemContent(user)
            }
        }
    }
}
