package io.getstream.chat.android.compose.ui.components.reactionoptions

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Icon
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.reactionoptions.ReactionOptionItemState
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Displays all available reactions.
 *
 * @param ownReactions A list of user's own reactions.
 * @param onReactionOptionSelected Handler that propagates click events on each item.
 * @param onShowMoreReactionsSelected Handler that propagates clicks on the show more button.
 * @param modifier Modifier for styling.
 * @param numberOfReactionsShown The maximum number of reactions shown before the show more reactions button is displayed.
 * @param horizontalArrangement Used for changing the arrangement.
 * @param reactionTypes All available reactions.
 * @param showMoreReactionsIcon Drawable resource used for the show more button.
 * @param itemContent Composable that allows the user to customize the individual items shown in [ReactionOptions].
 * By default shows individual reactions.
 */
@Composable
public fun ReactionOptions(
    ownReactions: List<Reaction>,
    onReactionOptionSelected: (ReactionOptionItemState) -> Unit,
    onShowMoreReactionsSelected: () -> Unit,
    modifier: Modifier = Modifier,
    numberOfReactionsShown: Int = DEFAULT_NUMBER_OF_REACTIONS_SHOWN,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    reactionTypes: Map<String, Int> = ChatTheme.reactionTypes,
    @DrawableRes showMoreReactionsIcon: Int = R.drawable.stream_compose_ic_more,
    itemContent: @Composable RowScope.(ReactionOptionItemState) -> Unit = { option ->
        DefaultReactionOptionItem(
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

    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement
    ) {
        options.take(numberOfReactionsShown).forEach { option ->
            key(option.type) {
                itemContent(option)
            }
        }

        if (options.size > numberOfReactionsShown) {
            Icon(
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = false),
                    onClick = { onShowMoreReactionsSelected() }
                ),
                painter = painterResource(id = showMoreReactionsIcon),
                contentDescription = LocalContext.current.getString(R.string.stream_compose_show_more_reactions),
                tint = ChatTheme.colors.textLowEmphasis,
            )
        }
    }
}

/**
 * The default reaction option item.
 *
 * @param option The represented option.
 * @param onReactionOptionSelected The handler when the option is selected.
 */
@Composable
internal fun DefaultReactionOptionItem(
    option: ReactionOptionItemState,
    onReactionOptionSelected: (ReactionOptionItemState) -> Unit,
) {
    ReactionOptionItem(
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = rememberRipple(bounded = false),
            onClick = { onReactionOptionSelected(option) }
        ),
        option = option
    )
}

/**
 * Preview of [ReactionOptions] with a single item selected.
 */
@Preview(showBackground = true, name = "ReactionOptions Preview")
@Composable
private fun ReactionOptionsPreview() {
    ChatTheme {
        val reactionType = ChatTheme.reactionTypes.keys.firstOrNull()

        if (reactionType != null)
            ReactionOptions(
                ownReactions = listOf(Reaction(reactionType)),
                onReactionOptionSelected = {},
                onShowMoreReactionsSelected = {}
            )
    }
}

private const val DEFAULT_NUMBER_OF_REACTIONS_SHOWN = 5
