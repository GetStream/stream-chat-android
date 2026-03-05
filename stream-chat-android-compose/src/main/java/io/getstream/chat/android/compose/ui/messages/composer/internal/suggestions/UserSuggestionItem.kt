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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import io.getstream.chat.android.compose.ui.components.avatar.AvatarSize
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.models.User

@Composable
internal fun UserSuggestionItem(
    user: User,
    currentUser: User?,
    onUserSelected: (User) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clickable { onUserSelected(user) }
            .padding(
                vertical = StreamTokens.spacingXs,
                horizontal = StreamTokens.spacingSm,
            )
            .testTag("Stream_UserSuggestionItem"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChatTheme.componentFactory.MessageComposerUserSuggestionItemLeadingContent(
            modifier = Modifier,
            user = user,
            currentUser = currentUser,
        )
        ChatTheme.componentFactory.MessageComposerUserSuggestionItemCenterContent(
            modifier = Modifier.weight(1f),
            user = user,
        )
        ChatTheme.componentFactory.MessageComposerUserSuggestionItemTrailingContent(
            modifier = Modifier,
            user = user,
        )
    }
}

@Composable
internal fun DefaultUserSuggestionItemLeadingContent(
    user: User,
    modifier: Modifier = Modifier,
) {
    ChatTheme.componentFactory.UserAvatar(
        modifier = modifier.size(AvatarSize.Medium),
        user = user,
        showIndicator = false,
        showBorder = true,
    )
}

@Composable
internal fun DefaultUserSuggestionItemCenterContent(
    modifier: Modifier,
    user: User,
) {
    Text(
        modifier = modifier.padding(start = StreamTokens.spacingSm),
        text = user.name,
        style = ChatTheme.typography.bodyDefault,
        color = ChatTheme.colors.textPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
