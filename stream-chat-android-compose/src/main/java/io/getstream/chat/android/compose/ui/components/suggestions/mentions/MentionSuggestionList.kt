package io.getstream.chat.android.compose.ui.components.suggestions.mentions

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.ui.components.suggestions.SuggestionList
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represents the mention suggestion list popup.
 *
 * @param users The list of users that will be displayed in the popup.
 * @param modifier Modifier for styling.
 * @param onMentionSelected Handler when the user taps on an item.
 * @param itemContent Customizable composable function that represents a single mention item.
 */
@Composable
public fun MentionSuggestionList(
    users: List<User>,
    modifier: Modifier = Modifier,
    onMentionSelected: (User) -> Unit = {},
    itemContent: @Composable (User) -> Unit = { user ->
        DefaultMentionSuggestionItem(
            user = user,
            onMentionSelected = onMentionSelected,
        )
    },
) {
    SuggestionList(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = ChatTheme.dimens.suggestionListMaxHeight)
    ) {
        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
            items(
                items = users,
                key = User::id
            ) { user ->
                itemContent(user)
            }
        }
    }
}
