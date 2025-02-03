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

package io.getstream.chat.android.compose.ui.components.reactionoptions

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.reactionoptions.ReactionOptionItemState
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.ReactionIcon
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
 * @param horizontalArrangement Used for changing the arrangement.
 * @param reactionTypes All available reactions.
 * @param showMoreReactionsIcon Drawable resource used for the show more button.
 * @param itemContent Composable that allows the user to customize the individual items shown in [ReactionOptions].
 * By default shows individual reactions.
 * @param showMore Composable that allows the user to customize the show more button.
 */
@Composable
public fun ReactionOptions(
    ownReactions: List<Reaction>,
    onReactionOptionSelected: (ReactionOptionItemState) -> Unit,
    onShowMoreReactionsSelected: () -> Unit,
    modifier: Modifier = Modifier,
    numberOfReactionsShown: Int = DefaultNumberOfReactionsShown,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    reactionTypes: Map<String, ReactionIcon> = ChatTheme.reactionIconFactory.createReactionIcons(),
    @DrawableRes showMoreReactionsIcon: Int = R.drawable.stream_compose_ic_more,
    itemContent: @Composable RowScope.(ReactionOptionItemState) -> Unit = { option ->
        with(ChatTheme.componentFactory) {
            ReactionMenuOptionItem(
                modifier = Modifier,
                option = option,
                onReactionOptionSelected = onReactionOptionSelected,
            )
        }
    },
    showMore: @Composable RowScope.(onShowMoreReactionsSelected: () -> Unit, showMoreReactionsIcon: Int) -> Unit = { par1, par2 ->
        with(ChatTheme.componentFactory) {
            ReactionMenuShowMore(Modifier, par1, par2)
        }
    },
) {
    val options = reactionTypes.entries.map { (type, reactionIcon) ->
        val isSelected = ownReactions.any { ownReaction -> ownReaction.type == type }
        val painter = reactionIcon.getPainter(isSelected)
        ReactionOptionItemState(
            painter = painter,
            type = type,
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
    ) {
        options.take(numberOfReactionsShown).forEach { option ->
            key(option.type) {
                itemContent(option)
            }
        }

        if (options.size > numberOfReactionsShown) {
            showMore(onShowMoreReactionsSelected, showMoreReactionsIcon)
        }
    }
}

/**
 * Preview of [ReactionOptions] with a single item selected.
 */
@Preview(showBackground = true, name = "ReactionOptions Preview")
@Composable
private fun ReactionOptionsPreview() {
    ChatTheme {
        val reactionType = ChatTheme.reactionIconFactory.createReactionIcons().keys.firstOrNull()

        if (reactionType != null) {
            ReactionOptions(
                ownReactions = listOf(Reaction(reactionType)),
                onReactionOptionSelected = {},
                onShowMoreReactionsSelected = {},
            )
        }
    }
}

/**
 * The default maximum number of reactions shown before the show more button.
 */
private const val DefaultNumberOfReactionsShown = 5
