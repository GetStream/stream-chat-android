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
import io.getstream.chat.android.compose.state.messages.reaction.ReactionOptionItemState
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Displays all available reactions in a [Row].
 *
 * @param ownReactions A list of user's own reactions. Each of these are by default highlighted using primaryAccent in [ChatTheme.colors].
 * @param modifier Compose UI [Modifier] that is applied to the internally used [Row].
 * @param horizontalArrangement Compose UI [Arrangement.Horizontal] that is applied to the internally used [Row].
 * @param reactionTypes A map of all available reactions that the user can click on.
 * @param onReactionOptionClicked Handler used to propagate click events that occur on individual [ReactionOptionItem].
 * @param itemContent Composable slot that represents an item inside the internally used [Row].
 */
@Composable
public fun ReactionOptions(
    ownReactions: List<Reaction>,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    reactionTypes: Map<String, Int> = ChatTheme.reactionTypes,
    onReactionOptionClicked: (ReactionOptionItemState) -> Unit,
    itemContent: @Composable RowScope.(ReactionOptionItemState) -> Unit = { reactionOptionItemState ->
        ReactionOptionItem(
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = false),
                onClick = { onReactionOptionClicked(reactionOptionItemState) }
            ),
            option = reactionOptionItemState)
    }
) {
    val reactionOptionItemStateList = reactionTypes.entries.map { (type, drawable) ->
        ReactionOptionItemState(
            painter = painterResource(id = drawable),
            isSelected = ownReactions.any { ownReaction -> ownReaction.type == type },
            type = type
        )
    }

    Row(modifier = modifier,
        horizontalArrangement = horizontalArrangement) {
        reactionOptionItemStateList.forEach { reactionOptionItemState ->
            itemContent(reactionOptionItemState)
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
                onReactionOptionClicked = {})
    }
}