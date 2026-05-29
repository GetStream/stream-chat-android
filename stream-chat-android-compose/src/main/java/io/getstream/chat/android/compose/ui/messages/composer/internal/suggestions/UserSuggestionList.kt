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

package io.getstream.chat.android.compose.ui.messages.composer.internal.suggestions

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageComposerSuggestionItemParams
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.Mention

@Composable
internal fun MentionSuggestionList(
    mentions: List<Mention>,
    currentUser: User? = null,
    onUserSelected: (User) -> Unit = {},
    onMentionSelected: (Mention) -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("Stream_MentionSuggestionList"),
        contentPadding = PaddingValues(vertical = StreamTokens.spacingXs),
    ) {
        itemsIndexed(
            items = mentions,
            key = ::mentionKey,
        ) { _, mention ->
            ChatTheme.componentFactory.MessageComposerSuggestionItem(
                params = MessageComposerSuggestionItemParams(
                    mention = mention,
                    currentUser = currentUser,
                    onMentionSelected = { selected ->
                        if (selected is Mention.User) onUserSelected(selected.user)
                        onMentionSelected(selected)
                    },
                ),
            )
        }
    }
}

private fun mentionKey(index: Int, mention: Mention): String = when (mention) {
    is Mention.User -> "user:${mention.user.id}"
    is Mention.Channel -> "channel"
    is Mention.Here -> "here"
    is Mention.Role -> "role:${mention.role}"
    is Mention.Group -> "group:${mention.group.id}"
    else -> "custom:${mention.type.value}:$index"
}

@Composable
@Preview(showBackground = true)
private fun MentionSuggestionListPreview() {
    ChatPreviewTheme {
        MentionSuggestionList(
            mentions = listOf(
                Mention.Channel,
                Mention.Here,
                Mention.User(PreviewUserData.user1),
                Mention.User(PreviewUserData.user2),
                Mention.Role("admin"),
            ),
        )
    }
}
