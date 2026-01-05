/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.compose.ui.components.suggestions.mentions

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.compose.ui.components.suggestions.SuggestionList
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewUserData

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
        ChatTheme.componentFactory.MessageComposerMentionSuggestionItem(
            user = user,
            onMentionSelected = onMentionSelected,
        )
    },
) {
    SuggestionList(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = ChatTheme.dimens.suggestionListMaxHeight),
    ) {
        MentionSuggestionLazyList(
            users = users,
            onMentionSelected = onMentionSelected,
            itemContent = itemContent,
        )
    }
}

@Composable
private fun MentionSuggestionLazyList(
    users: List<User>,
    onMentionSelected: (User) -> Unit = {},
    itemContent: @Composable (User) -> Unit = { user ->
        ChatTheme.componentFactory.MessageComposerMentionSuggestionItem(
            user = user,
            onMentionSelected = onMentionSelected,
        )
    },
) {
    LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
        items(
            items = users,
            key = User::id,
        ) { user ->
            itemContent(user)
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun MentionSuggestionListPreview() {
    ChatPreviewTheme {
        MentionSuggestionList()
    }
}

@Composable
internal fun MentionSuggestionList() {
    MentionSuggestionLazyList(
        users = listOf(
            PreviewUserData.user1,
            PreviewUserData.user2,
            PreviewUserData.user3,
        ),
    )
}
