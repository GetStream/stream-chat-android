package io.getstream.chat.android.compose.ui.components.reactionoptions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.compose.state.messages.reaction.ReactionOptionItemState
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme.reactionTypes

/**
 * Individual reaction item
 *
 * @param option The reaction to show.
 * @param onReactionClick Handler when the user clicks on the reaction.
 */
@Composable
internal fun ReactionOptionItem(
    option: ReactionOptionItemState,
    onReactionClick: (ReactionOptionItemState) -> Unit,
) {
    Icon(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = false),
                onClick = { onReactionClick(option) }
            )
            .padding(ChatTheme.dimens.reactionOptionsItemPadding),
        painter = option.painter,
        contentDescription = option.type,
        tint = if (option.isSelected) ChatTheme.colors.primaryAccent else ChatTheme.colors.textLowEmphasis
    )
}


/**
 * Preview of [ReactionOptionItem] in it's non selected state
 */
@Preview(showBackground = true, name = "ReactionOptionItem Preview (Not Selected)")
@Composable
private fun ReactionOptionItemNotSelectedPreview() {
    ChatTheme {
        val reactionOptionItem = reactionTypes.entries.map { (type, drawable) ->
            ReactionOptionItemState(
                painter = painterResource(id = drawable),
                isSelected = false,
                type = type
            )
        }

        reactionOptionItem.firstOrNull()?.let {
            ReactionOptionItem(option = it, onReactionClick = {})
        }
    }
}

/**
 * Preview of [ReactionOptionItem] in it's selected state
 */
@Preview(showBackground = true, name = "ReactionOptionItem Preview (Selected)")
@Composable
private fun ReactionOptionItemSelectedPreview() {
    ChatTheme {
        val reactionOptionItem = reactionTypes.entries.map { (type, drawable) ->
            ReactionOptionItemState(
                painter = painterResource(id = drawable),
                isSelected = true,
                type = type
            )
        }

        reactionOptionItem.firstOrNull()?.let {
            ReactionOptionItem(option = it, onReactionClick = {})
        }
    }
}