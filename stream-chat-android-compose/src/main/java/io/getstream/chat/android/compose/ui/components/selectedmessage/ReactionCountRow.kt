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

package io.getstream.chat.android.compose.ui.components.selectedmessage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.previewdata.PreviewReactionData
import io.getstream.chat.android.compose.state.messages.MessageReactionItemState
import io.getstream.chat.android.compose.ui.components.reactions.ReactionIconSize
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.applyIf
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.models.Reaction

/**
 * Horizontally scrollable row of reaction chips with counts.
 *
 * Each chip shows the reaction emoji and its count (when > 1). Chips for reactions the current
 * user has added are rendered with a selected background; all others use a default surface style.
 * Tapping a chip toggles the current user's reaction of that type.
 *
 * @param reactionGroups The list of reaction types with their emoji and count.
 * @param ownReactions The current user's reactions on this message (used to determine selected state).
 * @param onReactionOptionSelected Called with the reaction type when a chip is tapped.
 * @param onAddReactionClick Called when the add-reaction chip is tapped, or null to hide it.
 * @param modifier Modifier for styling.
 */
@Composable
internal fun ReactionCountRow(
    reactionGroups: List<MessageReactionItemState>,
    ownReactions: List<Reaction>,
    onReactionOptionSelected: (String) -> Unit,
    onAddReactionClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = StreamTokens.spacingMd, vertical = StreamTokens.spacingXs)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onAddReactionClick != null) {
            ReactionChip(
                checked = false,
                onClick = onAddReactionClick,
                count = null,
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.stream_compose_ic_reaction_add),
                        contentDescription = stringResource(R.string.stream_compose_reactions_add),
                        tint = ChatTheme.colors.chipText,
                    )
                },
            )
        }

        reactionGroups.forEach { reaction ->
            ReactionChip(
                checked = ownReactions.any { it.type == reaction.type },
                onClick = { onReactionOptionSelected(reaction.type) },
                count = reaction.count,
                icon = {
                    ChatTheme.componentFactory.ReactionIcon(
                        type = reaction.type,
                        emoji = reaction.emoji,
                        size = ReactionIconSize.Small,
                        modifier = Modifier,
                    )
                },
            )
        }
    }
}

@Composable
private fun ReactionChip(
    checked: Boolean,
    onClick: () -> Unit,
    count: Int?,
    icon: @Composable () -> Unit,
) {
    val colors = ChatTheme.colors
    Row(
        modifier = Modifier
            .applyIf(checked) { background(colors.backgroundCoreSelected, CircleShape) }
            .border(1.dp, color = colors.borderCoreDefault, shape = CircleShape)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .defaultMinSize(minWidth = 64.dp, minHeight = 32.dp)
            .padding(horizontal = StreamTokens.spacingSm, vertical = StreamTokens.spacingXs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs, Alignment.CenterHorizontally),
        content = {
            icon()
            count?.toString()?.let {
                Text(
                    text = it,
                    style = ChatTheme.typography.bodyEmphasis,
                    color = colors.chipText,
                )
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun ReactionCountRowPreview() {
    ChatTheme {
        ReactionCountRow(
            reactionGroups = PreviewReactionData.manyReactions(),
            ownReactions = listOf(Reaction(type = "love")),
            onReactionOptionSelected = {},
            onAddReactionClick = {},
        )
    }
}
