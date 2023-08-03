/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.User

/**
 * Represents the mention suggestion item in the mention suggestion list popup.
 *
 * @param user The user that will be used to autocomplete the mention.
 * @param modifier Modifier for styling.
 * @param onMentionSelected Handler when the user taps on an item.
 * @param leadingContent Customizable composable function that represents the leading content of a mention item.
 * @param centerContent Customizable composable function that represents the center content of a mention item.
 * @param trailingContent Customizable composable function that represents the trailing content of the a mention item.
 */
@Composable
public fun MentionSuggestionItem(
    user: User,
    onMentionSelected: (User) -> Unit,
    modifier: Modifier = Modifier,
    leadingContent: @Composable RowScope.(User) -> Unit = {
        DefaultMentionSuggestionItemLeadingContent(user = it)
    },
    centerContent: @Composable RowScope.(User) -> Unit = {
        DefaultMentionSuggestionItemCenterContent(user = it)
    },
    trailingContent: @Composable RowScope.(User) -> Unit = {
        DefaultMentionSuggestionItemTrailingContent()
    },
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable(
                onClick = { onMentionSelected(user) },
                indication = rememberRipple(),
                interactionSource = remember { MutableInteractionSource() },
            )
            .padding(
                vertical = ChatTheme.dimens.mentionSuggestionItemVerticalPadding,
                horizontal = ChatTheme.dimens.mentionSuggestionItemHorizontalPadding,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingContent(user)

        centerContent(user)

        trailingContent(user)
    }
}

/**
 * Represents the default content shown at the start of the mention list item.
 *
 * @param user The user item to show the content for.
 */
@Composable
internal fun DefaultMentionSuggestionItemLeadingContent(user: User) {
    UserAvatar(
        modifier = Modifier
            .padding(end = 8.dp)
            .size(ChatTheme.dimens.mentionSuggestionItemAvatarSize),
        user = user,
        showOnlineIndicator = true,
    )
}

/**
 *  Represents the center portion of the mention item, that show the user name and the user ID.
 *
 *  @param user The user to show the info for.
 */
@Composable
internal fun RowScope.DefaultMentionSuggestionItemCenterContent(user: User) {
    Column(
        modifier = Modifier
            .weight(1f)
            .wrapContentHeight(),
    ) {
        Text(
            text = user.name,
            style = ChatTheme.typography.bodyBold,
            color = ChatTheme.colors.textHighEmphasis,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "@${user.id}",
            style = ChatTheme.typography.footnote,
            color = ChatTheme.colors.textLowEmphasis,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Represents the default content shown at the end of the mention list item.
 */
@Composable
internal fun DefaultMentionSuggestionItemTrailingContent() {
    Icon(
        modifier = Modifier
            .padding(start = 8.dp)
            .size(24.dp),
        painter = painterResource(id = R.drawable.stream_compose_ic_mention),
        contentDescription = null,
        tint = ChatTheme.colors.primaryAccent,
    )
}
