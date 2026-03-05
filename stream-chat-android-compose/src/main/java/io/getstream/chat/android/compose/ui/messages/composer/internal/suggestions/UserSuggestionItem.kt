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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.utils.extensions.shouldShowOnlineIndicator

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
            .wrapContentHeight()
            .clickable { onUserSelected(user) }
            .padding(
                vertical = StreamTokens.spacingXs,
                horizontal = StreamTokens.spacingMd,
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

/**
 * Represents the default content shown at the start of the mention list item.
 */
@Composable
internal fun DefaultUserSuggestionItemLeadingContent(
    user: User,
    currentUser: User?,
) {
    ChatTheme.componentFactory.UserAvatar(
        modifier = Modifier
            .padding(end = 8.dp)
            .size(40.dp),
        user = user,
        showIndicator = user.shouldShowOnlineIndicator(
            userPresence = ChatTheme.userPresence,
            currentUser = currentUser,
        ),
        showBorder = false,
    )
}

/**
 *  Represents the center portion of the mention item, that show the user name and the user ID.
 */
@Composable
internal fun DefaultUserSuggestionItemCenterContent(
    modifier: Modifier,
    user: User,
) {
    Column(modifier = modifier) {
        val username = "@${user.id}"
        Text(
            text = user.name.ifEmpty { username },
            style = ChatTheme.typography.bodyEmphasis,
            color = ChatTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (user.name.isNotEmpty()) {
            Text(
                text = username,
                style = ChatTheme.typography.bodyDefault,
                color = ChatTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Represents the default content shown at the end of the mention list item.
 */
@Composable
internal fun DefaultUserSuggestionItemTrailingContent() {
    Icon(
        modifier = Modifier
            .padding(start = 8.dp)
            .size(24.dp),
        painter = painterResource(id = R.drawable.stream_compose_ic_mentions),
        contentDescription = null,
        tint = ChatTheme.colors.accentPrimary,
    )
}
