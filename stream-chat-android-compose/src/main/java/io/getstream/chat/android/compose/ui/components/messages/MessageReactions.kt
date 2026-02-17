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

package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.previewdata.PreviewReactionData
import io.getstream.chat.android.compose.state.messages.MessageReactionItemState
import io.getstream.chat.android.compose.ui.components.reactions.ReactionIconSize
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.compose.ui.util.ifNotNull

/**
 * Adaptive message reactions that use [SegmentedMessageReactions] by default, falling back to
 * [ClusteredMessageReactions] when the segmented layout would exceed the available width.
 *
 * @param reactions The list of reactions to display.
 * @param modifier Modifier for styling.
 * @param onClick Handler when the user taps on the reaction pills.
 */
@Composable
public fun AdaptiveMessageReactions(
    reactions: List<MessageReactionItemState>,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val unconstrainedConstraints = constraints.copy(maxWidth = Int.MAX_VALUE)
        val segmentedIntrinsic = subcompose("segmented_intrinsic") {
            SegmentedMessageReactions(reactions = reactions, onClick = onClick)
        }.first().measure(unconstrainedConstraints)

        if (segmentedIntrinsic.width > constraints.maxWidth) {
            val clustered = subcompose("clustered") {
                ClusteredMessageReactions(reactions = reactions, onClick = onClick)
            }.first().measure(constraints)

            layout(clustered.width, clustered.height) {
                clustered.place(0, 0)
            }
        } else {
            val segmented = subcompose("segmented") {
                SegmentedMessageReactions(reactions = reactions, onClick = onClick)
            }.first().measure(constraints)

            layout(segmented.width, segmented.height) {
                segmented.place(0, 0)
            }
        }
    }
}

/**
 * Represents a reaction bubble with a list of reactions this message has.
 *
 * @param reactions The list of reactions to display.
 * @param modifier Modifier for styling.
 * @param onClick Handler when the reaction list is clicked.
 */
@Composable
public fun ClusteredMessageReactions(
    reactions: List<MessageReactionItemState>,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val description = pluralStringResource(
        R.plurals.stream_ui_message_list_semantics_message_reactions,
        reactions.size,
        reactions.size,
    )
    val colors = ChatTheme.colors
    val count = reactions.sumOf(MessageReactionItemState::count)

    Row(
        modifier = modifier
            .semantics {
                testTag = "Stream_MessageReaction"
                contentDescription = description
            }
            .background(colors.barsBackground, CircleShape)
            .border(1.dp, color = colors.borderCoreSurfaceSubtle, shape = CircleShape)
            .ifNotNull(onClick) { clip(CircleShape).clickable(onClick = it) }
            .padding(horizontal = StreamTokens.spacingXs, vertical = StreamTokens.spacing2xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs),
    ) {
        reactions.forEach {
            ChatTheme.componentFactory.ReactionIcon(
                type = it.type,
                emoji = it.emoji,
                size = ReactionIconSize.Small,
                modifier = Modifier.testTag("Stream_MessageReaction_${it.type}"),
            )
        }
        if (count > 1) {
            Text(
                text = count.toString(),
                style = ChatTheme.typography.numericMedium,
                color = colors.textPrimary,
            )
        }
    }
}

/**
 * Represents a segmented reaction layout where each reaction gets its own pill with an individual count.
 *
 * @param reactions The list of reactions to display.
 * @param modifier Modifier for styling.
 * @param onClick Handler when the user taps on the reaction pills.
 */
@Composable
public fun SegmentedMessageReactions(
    reactions: List<MessageReactionItemState>,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val description = pluralStringResource(
        R.plurals.stream_ui_message_list_semantics_message_reactions,
        reactions.size,
        reactions.size,
    )
    val colors = ChatTheme.colors
    val shouldShowCounts = reactions.any { it.count > 1 }

    Row(
        modifier = modifier.semantics {
            testTag = "Stream_MessageReaction"
            contentDescription = description
        },
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs),
    ) {
        reactions.forEach {
            Row(
                modifier = Modifier
                    .background(colors.barsBackground, CircleShape)
                    .border(1.dp, color = colors.borderCoreSurfaceSubtle, shape = CircleShape)
                    .ifNotNull(onClick) { clip(CircleShape).clickable(onClick = it) }
                    .padding(horizontal = StreamTokens.spacingXs, vertical = StreamTokens.spacing2xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs),
            ) {
                ChatTheme.componentFactory.ReactionIcon(
                    type = it.type,
                    emoji = it.emoji,
                    size = ReactionIconSize.Small,
                    modifier = Modifier.testTag("Stream_MessageReaction_${it.type}"),
                )
                if (shouldShowCounts) {
                    Text(
                        text = it.count.toString(),
                        style = ChatTheme.typography.numericMedium,
                        color = colors.textPrimary,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun SingleClusteredMessageReactionsPreview() {
    ChatTheme {
        ClusteredMessageReactions(reactions = PreviewReactionData.oneReaction())
    }
}

@Preview
@Composable
private fun MultipleClusteredMessageReactionsPreview() {
    ChatTheme {
        ClusteredMessageReactions(reactions = PreviewReactionData.manyReactions())
    }
}

@Preview
@Composable
private fun SingleSegmentedMessageReactionsPreview() {
    ChatTheme {
        SegmentedMessageReactions(reactions = PreviewReactionData.oneReaction())
    }
}

@Preview
@Composable
private fun MultipleSegmentedMessageReactionsPreview() {
    ChatTheme {
        SegmentedMessageReactions(reactions = PreviewReactionData.manyReactions())
    }
}
