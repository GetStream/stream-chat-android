package io.getstream.chat.android.compose.ui.components.reactionoptions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.compose.state.reactionoptions.ReactionOptionItemState
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Displays all available reactions.
 *
 * @param ownReactions A list of user's own reactions.
 * @param onReactionOptionSelected Handler that propagates click events on each item.
 * @param modifier Modifier for styling.
 * @param horizontalArrangement Used for changing the arrangement.
 * @param reactionTypes All available reactions.
 * @param itemContent Composable that allows the user to customize the individual items shown in [ReactionOptions].
 * By default shows individual reactions.
 */
@Composable
public fun ReactionOptions(
    ownReactions: List<Reaction>,
    onReactionOptionSelected: (ReactionOptionItemState) -> Unit,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    reactionTypes: Map<String, Int> = ChatTheme.reactionTypes,
    itemContent: @Composable RowScope.(ReactionOptionItemState) -> Unit = { option ->
        ReactionOptionItem(
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = false),
                onClick = { onReactionOptionSelected(option) }
            ),
            option = option
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
        options.forEach { option ->
            itemContent(option)
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
        val reactionType = ChatTheme.reactionTypes.keys.firstOrNull()

        if (reactionType != null)
            ReactionOptions(
                ownReactions = listOf(Reaction(reactionType)),
                onReactionOptionSelected = {}
            )
    }
}
