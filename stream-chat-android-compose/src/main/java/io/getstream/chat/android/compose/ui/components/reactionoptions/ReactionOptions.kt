package io.getstream.chat.android.compose.ui.components.reactionoptions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.compose.state.messages.reaction.ReactionOptionItemState
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Displays all available reactions in a [Row]
 *
 * @param ownReactions a [List] of user's own reactions. Each of these are by default highlighted using [ChatTheme.colors.primaryAccent]
 * @param modifier Compose UI [Modifier] that is applied to the internally used [Row]
 * @param horizontalArrangement Compose UI [Arrangement.Horizontal] that is applied to the internally used [Row]
 * @param reactionTypes a [Map] of all available reactions that the user can click on
 * @param onReactionOptionClicked Handler used to propagate click events that occur on individual [ReactionOptionItem]
 */
@Composable
public fun ReactionOptions(
    ownReactions: List<Reaction>,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    reactionTypes: Map<String, Int> = ChatTheme.reactionTypes,
    onReactionOptionClicked: (ReactionOptionItemState) -> Unit,
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
            ReactionOptionItem(option = reactionOptionItemState, onReactionClick = onReactionOptionClicked)
        }
    }
}

/**
 * Preview of [ReactionOptions] with a single item selected
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