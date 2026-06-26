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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.avatar.AvatarSize
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageComposerSuggestionItemCenterContentParams
import io.getstream.chat.android.compose.ui.theme.MessageComposerSuggestionItemLeadingContentParams
import io.getstream.chat.android.compose.ui.theme.MessageComposerSuggestionItemTrailingContentParams
import io.getstream.chat.android.compose.ui.theme.MessageComposerUserSuggestionItemCenterContentParams
import io.getstream.chat.android.compose.ui.theme.MessageComposerUserSuggestionItemLeadingContentParams
import io.getstream.chat.android.compose.ui.theme.MessageComposerUserSuggestionItemTrailingContentParams
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.theme.UserAvatarParams
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.Mention
import io.getstream.chat.android.ui.common.R as UiCommonR

/**
 * Default impl of [io.getstream.chat.android.compose.ui.theme.ChatComponentFactory.MessageComposerUserSuggestionItem].
 * Wires user-specific factory slots into [SuggestionItemRow].
 */
@Composable
@Suppress("DEPRECATION")
internal fun UserSuggestionItem(
    user: User,
    currentUser: User?,
    onUserSelected: (User) -> Unit,
    modifier: Modifier = Modifier,
) {
    SuggestionItemRow(
        modifier = modifier,
        onClick = { onUserSelected(user) },
        leadingContent = {
            ChatTheme.componentFactory.MessageComposerUserSuggestionItemLeadingContent(
                params = MessageComposerUserSuggestionItemLeadingContentParams(
                    user = user,
                    currentUser = currentUser,
                ),
            )
        },
        centerContent = {
            ChatTheme.componentFactory.MessageComposerUserSuggestionItemCenterContent(
                params = MessageComposerUserSuggestionItemCenterContentParams(
                    modifier = Modifier.weight(1f),
                    user = user,
                ),
            )
        },
        trailingContent = {
            ChatTheme.componentFactory.MessageComposerUserSuggestionItemTrailingContent(
                params = MessageComposerUserSuggestionItemTrailingContentParams(
                    user = user,
                ),
            )
        },
    )
}

/**
 * Default impl of [io.getstream.chat.android.compose.ui.theme.ChatComponentFactory.MessageComposerSuggestionItem].
 * Wires mention-specific factory slots into [SuggestionItemRow].
 */
@Composable
internal fun MentionSuggestionItem(
    mention: Mention,
    onMentionSelected: (Mention) -> Unit,
    modifier: Modifier = Modifier,
) {
    SuggestionItemRow(
        modifier = modifier,
        onClick = { onMentionSelected(mention) },
        leadingContent = {
            ChatTheme.componentFactory.MessageComposerSuggestionItemLeadingContent(
                params = MessageComposerSuggestionItemLeadingContentParams(
                    mention = mention,
                ),
            )
        },
        centerContent = {
            ChatTheme.componentFactory.MessageComposerSuggestionItemCenterContent(
                params = MessageComposerSuggestionItemCenterContentParams(
                    modifier = Modifier.weight(1f),
                    mention = mention,
                ),
            )
        },
        trailingContent = {
            ChatTheme.componentFactory.MessageComposerSuggestionItemTrailingContent(
                params = MessageComposerSuggestionItemTrailingContentParams(
                    mention = mention,
                ),
            )
        },
    )
}

@Composable
internal fun SuggestionItemRow(
    onClick: () -> Unit,
    leadingContent: @Composable () -> Unit,
    centerContent: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clickable { onClick() }
            .padding(
                vertical = StreamTokens.spacingXs,
                horizontal = StreamTokens.spacingSm,
            )
            .testTag("Stream_SuggestionItem"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingContent()
        centerContent()
        trailingContent()
    }
}

@Composable
internal fun DefaultMentionSuggestionItemLeadingContent(
    mention: Mention,
    modifier: Modifier = Modifier,
) {
    when (mention) {
        is Mention.User -> ChatTheme.componentFactory.UserAvatar(
            params = UserAvatarParams(
                modifier = modifier.size(AvatarSize.Medium),
                user = mention.user,
                showBorder = true,
            ),
        )
        Mention.Channel, Mention.Here -> MentionIconAvatar(
            modifier = modifier,
            iconRes = UiCommonR.drawable.stream_design_ic_megaphone,
        )
        is Mention.Role -> MentionIconAvatar(
            modifier = modifier,
            iconRes = UiCommonR.drawable.stream_design_ic_role,
        )
        is Mention.Group -> MentionIconAvatar(
            modifier = modifier,
            iconRes = R.drawable.stream_design_ic_users,
        )
        else -> Spacer(Modifier.size(AvatarSize.Medium))
    }
}

@Composable
internal fun DefaultMentionSuggestionItemCenterContent(
    modifier: Modifier,
    mention: Mention,
) {
    if (mention is Mention.User) {
        Text(
            modifier = modifier.padding(start = StreamTokens.spacingSm),
            text = mention.user.name,
            style = ChatTheme.typography.bodyDefault,
            color = ChatTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        return
    }
    Column(
        modifier = modifier.padding(start = StreamTokens.spacingSm),
        verticalArrangement = Arrangement.spacedBy(StreamTokens.spacing3xs),
    ) {
        Text(
            text = "@${mention.display}",
            style = ChatTheme.typography.bodyDefault,
            color = ChatTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        mentionSubtitle(mention)?.let { subtitle ->
            Text(
                text = subtitle,
                style = ChatTheme.typography.metadataDefault,
                color = ChatTheme.colors.textTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Wraps a 16dp icon in the circular surface-subtle "avatar" treatment used for non-user mention
 * suggestions, sized to match [AvatarSize.Medium] so the popup row aligns with user mentions.
 */
@Composable
private fun MentionIconAvatar(
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(AvatarSize.Medium)
            .clip(CircleShape)
            .background(ChatTheme.colors.backgroundCoreSurfaceSubtle)
            .border(width = 1.dp, color = ChatTheme.colors.borderCoreSubtle, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = ChatTheme.colors.textPrimary,
        )
    }
}

@Composable
private fun mentionSubtitle(mention: Mention): String? = when (mention) {
    Mention.Channel -> stringResource(
        id = R.string.stream_compose_message_composer_mention_suggestion_channel_subtitle,
    )

    Mention.Here -> stringResource(
        id = R.string.stream_compose_message_composer_mention_suggestion_here_subtitle,
    )

    is Mention.Role -> stringResource(
        id = R.string.stream_compose_message_composer_mention_suggestion_role_subtitle,
        mention.role,
    )

    is Mention.Group -> mention.group.description?.takeIf { it.isNotBlank() }

    else -> null
}
