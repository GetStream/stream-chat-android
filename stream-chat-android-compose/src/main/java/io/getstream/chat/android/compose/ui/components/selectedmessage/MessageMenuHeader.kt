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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.state.reactionoptions.ReactionOptionItemState
import io.getstream.chat.android.compose.state.userreactions.ReactionItem
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.models.Reaction

/**
 * Displays all available reactions.
 *
 * @param ownReactions A list of user's own reactions.
 * @param onReactionOptionSelected Handler that propagates click events on each item.
 * @param onShowMoreReactionsSelected Handler that propagates clicks on the show more button.
 * @param modifier Modifier for styling.
 * @param numberOfReactionsShown The maximum number of reactions shown before the show more reactions button is
 * displayed.
 * @param reactionTypes All available reactions.
 */
@Composable
// TODO [G.] move file
public fun MessageMenuHeader(
    ownReactions: List<Reaction>,
    onReactionOptionSelected: (ReactionOptionItemState) -> Unit,
    onShowMoreReactionsSelected: () -> Unit,
    modifier: Modifier = Modifier,
    numberOfReactionsShown: Int = DefaultNumberOfReactionsShown,
    reactionTypes: Map<String, String> = ChatTheme.reactionProvider.availableReactions,
) {
    val componentFactory = ChatTheme.componentFactory
    val colors = ChatTheme.colors
    val options = reactionTypes.entries.map { (type, emoji) ->
        ReactionOptionItemState(
            item = ReactionItem(type = type, emoji = emoji),
            isSelected = ownReactions.any { ownReaction -> ownReaction.type == type },
        )
    }

    Row(
        modifier = modifier
            .background(colors.backgroundElevationElevation2, CircleShape)
            .border(1.dp, colors.borderCoreDefault, CircleShape)
            .padding(StreamTokens.spacing2xs),
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacing3xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.take(numberOfReactionsShown).forEach { option ->
            componentFactory.ReactionMenuOptionItem(
                modifier = Modifier,
                option = option,
                onReactionOptionSelected = onReactionOptionSelected,
            )
        }

        if (options.size > numberOfReactionsShown) {
            componentFactory.ReactionMenuShowMore(Modifier, onShowMoreReactionsSelected)
        }
    }
}

/**
 * Preview of [MessageMenuHeader] with a single item selected.
 */
@Preview(showBackground = true, name = "ReactionOptions Preview")
@Composable
private fun MessageMenuHeaderPreview() {
    ChatTheme {
        val reactionType = ChatTheme.reactionProvider.availableReactions.keys.firstOrNull()

        if (reactionType != null) {
            MessageMenuHeader(
                ownReactions = listOf(Reaction(reactionType)),
                numberOfReactionsShown = 4,
                onReactionOptionSelected = {},
                onShowMoreReactionsSelected = {},
            )
        }
    }
}

/**
 * The default maximum number of reactions shown before the show more button.
 */
private const val DefaultNumberOfReactionsShown = 3
