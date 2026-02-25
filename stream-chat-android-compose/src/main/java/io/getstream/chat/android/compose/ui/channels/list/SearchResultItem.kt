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

package io.getstream.chat.android.compose.ui.channels.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.compose.ui.components.Timestamp
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.utils.extensions.shouldShowOnlineIndicator

/**
 * The basic search result item that show the message and the channel name in a list and expose click actions.
 *
 * @param searchResultItemState The state of the search result item.
 * @param currentUser The currently logged in user.
 * @param modifier Modifier for styling.
 * @param onSearchResultClick The optional action to execute when the item is clicked.
 * @param leadingContent Customizable composable function that represents the leading content of a search result item,
 * usually the avatar that holds an image of the user that sent the message.
 * @param centerContent Customizable composable function that represents the center content of a search result item,
 * usually holding information about the message and who and where it was sent.
 * @param trailingContent Customizable composable function that represents the trailing content of a search result item,
 * usually information about the date where the message was sent.
 */
@Composable
public fun SearchResultItem(
    searchResultItemState: ItemState.SearchResultItemState,
    currentUser: User?,
    modifier: Modifier = Modifier,
    onSearchResultClick: ((Message) -> Unit)? = null,
    leadingContent: @Composable RowScope.(ItemState.SearchResultItemState) -> Unit = {
        with(ChatTheme.componentFactory) {
            SearchResultItemLeadingContent(it, currentUser)
        }
    },
    centerContent: @Composable RowScope.(ItemState.SearchResultItemState) -> Unit = {
        with(ChatTheme.componentFactory) {
            SearchResultItemCenterContent(it, currentUser)
        }
    },
    trailingContent: @Composable RowScope.(ItemState.SearchResultItemState) -> Unit = {
        with(ChatTheme.componentFactory) {
            SearchResultItemTrailingContent(it)
        }
    },
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .run {
                if (onSearchResultClick != null) {
                    clickable { onSearchResultClick(searchResultItemState.message) }
                } else {
                    this
                }
            },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingMd), // 16dp avatar-to-content gap
        ) {
            leadingContent(searchResultItemState)
            centerContent(searchResultItemState)
            trailingContent(searchResultItemState)
        }
    }
}

/**
 * The default leading content of a search result item, usually the avatar that holds an image of the user that sent
 * the message.
 *
 * @param searchResultItemState The state of the search result item.
 * @param currentUser The currently logged in user.
 */
@Composable
internal fun DefaultSearchResultItemLeadingContent(
    searchResultItemState: ItemState.SearchResultItemState,
    currentUser: User?,
) {
    (
        searchResultItemState
            .channel
            ?.takeIf { it.members.size == 2 }
            ?.let { it.members.firstOrNull { it.getUserId() != currentUser?.id }?.user }
            ?: searchResultItemState.message.user
        )
        .let { user ->
            ChatTheme.componentFactory.UserAvatar(
                user = user,
                modifier = Modifier
                    .padding(
                        start = StreamTokens.spacingMd,
                        end = 0.dp,
                        top = StreamTokens.spacingMd,
                        bottom = StreamTokens.spacingMd,
                    )
                    .size(48.dp),
                showIndicator = user.shouldShowOnlineIndicator(
                    userPresence = ChatTheme.userPresence,
                    currentUser = currentUser,
                ),
                showBorder = false,
            )
        }
}

/**
 * The default center content of a search result item, usually holding information about the message and who and where
 * it was sent.
 *
 * @param searchResultItemState The state of the search result item.
 * @param currentUser The currently logged in user.
 */
@Composable
internal fun RowScope.DefaultSearchResultItemCenterContent(
    searchResultItemState: ItemState.SearchResultItemState,
    currentUser: User?,
) {
    Column(
        modifier = Modifier
            .padding(
                start = StreamTokens.spacing2xs, // 4dp (was 4.dp, same value but token-aligned)
                end = StreamTokens.spacing2xs, // 4dp (was 4.dp)
                top = StreamTokens.spacing3xs, // 2dp vertical padding
                bottom = StreamTokens.spacing3xs, // 2dp
            )
            .weight(1f)
            .wrapContentHeight(),
        verticalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs), // 4dp gap
    ) {
        Text(
            text = ChatTheme.searchResultNameFormatter.formatMessageTitle(searchResultItemState, currentUser),
            style = ChatTheme.typography.bodyDefault,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = ChatTheme.colors.textPrimary,
        )

        Text(
            text = ChatTheme.messagePreviewFormatter.formatMessagePreview(
                searchResultItemState.message,
                currentUser,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = ChatTheme.typography.captionDefault,
            color = ChatTheme.colors.textSecondary,
        )
    }
}

/**
 * The default trailing content of a search result item, usually information about the date where the message was sent.
 *
 * @param searchResultItemState The state of the search result item.
 */
@Composable
internal fun RowScope.DefaultSearchResultItemTrailingContent(
    searchResultItemState: ItemState.SearchResultItemState,
) {
    Column(
        modifier = Modifier
            .padding(
                start = StreamTokens.spacing2xs,
                end = StreamTokens.spacingMd,
                top = StreamTokens.spacingMd,
                bottom = StreamTokens.spacingMd,
            )
            .wrapContentHeight()
            .align(Alignment.CenterVertically), // was Alignment.Bottom; center-align with Figma layout
        horizontalAlignment = Alignment.End,
    ) {
        Timestamp(
            date = searchResultItemState.message.createdAt,
            textStyle = ChatTheme.typography.captionDefault.copy( // was default footnote
                color = ChatTheme.colors.textTertiary, // was textLowEmphasis
                lineHeight = 20.sp, // Figma: line-height/normal = 20sp
            ),
        )
    }
}
