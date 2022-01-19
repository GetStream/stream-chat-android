package io.getstream.chat.android.compose.ui.components.reactionoptions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyGridScope
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.compose.state.reactionoptions.ReactionOptionItemState
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Displays all available reactions a user can set on a message.
 *
 * @param ownReactions A list of user's own reactions.
 * @param onReactionOptionSelected Handler that propagates click events on each item.
 * @param modifier Modifier for styling.
 * @param cells Describes the way cells are formed inside [ExtendedReactionsOptions].
 * @param reactionTypes All available reactions.
 * @param itemContent Composable that allows the user to customize the individual items shown in [ExtendedReactionsOptions].
 * By default it shows individual reactions.
 */
@ExperimentalFoundationApi
@Composable
public fun ExtendedReactionsOptions(
    ownReactions: List<Reaction>,
    onReactionOptionSelected: (ReactionOptionItemState) -> Unit,
    modifier: Modifier = Modifier,
    cells: GridCells = GridCells.Fixed(5),
    reactionTypes: Map<String, Int> = ChatTheme.reactionTypes,
    itemContent: @Composable LazyGridScope.(ReactionOptionItemState) -> Unit = { option ->
        DefaultExtendedReactionsItemContent(
            option = option,
            onReactionOptionSelected = onReactionOptionSelected
        )
    },
) {
    val options = reactionTypes.entries.map { (type, drawable) ->
        ReactionOptionItemState(
            painter = painterResource(id = drawable),
            isSelected = ownReactions.any { ownReaction -> ownReaction.type == type },
            type = type
        )
    }

    LazyVerticalGrid(modifier = modifier, cells = cells) {
        items(options) { item ->
            key(item.type) {
                itemContent(item)
            }
        }
    }
}

/**
 * The default item content inside [ExtendedReactionsOptions]. Shows an individual reaction.
 *
 * @param option Individual reaction option.
 * @param onReactionOptionSelected Handler that propagates click events on each item.
 */
@Composable
internal fun DefaultExtendedReactionsItemContent(
    option: ReactionOptionItemState,
    onReactionOptionSelected: (ReactionOptionItemState) -> Unit,
) {
    ReactionOptionItem(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = false),
                onClick = { onReactionOptionSelected(option) }
            ),
        option = option
    )
}

/**
 * Preview for [ExtendedReactionsOptions] with no reaction selected.
 */
@ExperimentalFoundationApi
@Preview(showBackground = true, name = "ExtendedReactionOptions Preview")
@Composable
internal fun ExtendedReactionOptionsPreview() {
    ChatTheme {
        ExtendedReactionsOptions(
            ownReactions = listOf(),
            onReactionOptionSelected = {}
        )
    }
}

/**
 * Preview for [ExtendedReactionsOptions] with a selected reaction.
 */
@ExperimentalFoundationApi
@Preview(showBackground = true, name = "ExtendedReactionOptions Preview (With Own Reaction)")
@Composable
internal fun ExtendedReactionOptionsWithOwnReactionPreview() {
    ChatTheme {
        ExtendedReactionsOptions(
            ownReactions = listOf(
                Reaction(
                    messageId = "messageId",
                    type = "haha"
                )
            ),
            onReactionOptionSelected = {}
        )
    }
}
