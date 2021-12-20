package io.getstream.chat.android.compose.ui.components.userreactions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.previewdata.PreviewUserReactionData
import io.getstream.chat.android.compose.state.userreactions.UserReactionItemState
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represent a section with a list of reactions left for the message.
 *
 * @param items The list of user reactions to display.
 * @param modifier Modifier for styling.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun UserReactions(
    items: List<UserReactionItemState>,
    modifier: Modifier = Modifier,
    itemContent: @Composable (UserReactionItemState) -> Unit = {
        UserReactionItem(
            item = it,
            modifier = Modifier,
        )
    },
) {
    val reactionCount = items.size

    val reactionCountText = LocalContext.current.resources.getQuantityString(
        R.plurals.stream_compose_message_reactions,
        reactionCount,
        reactionCount
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.background(ChatTheme.colors.barsBackground)
    ) {
        Text(
            text = reactionCountText,
            style = ChatTheme.typography.title3Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = ChatTheme.colors.textHighEmphasis
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (reactionCount > 0) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val reactionItemWidth = 80.dp
                val maxColumns = maxOf((maxWidth / reactionItemWidth).toInt(), 1)
                val columns = reactionCount.coerceAtMost(maxColumns)
                val reactionGridWidth = reactionItemWidth * columns

                LazyVerticalGrid(
                    modifier = Modifier
                        .width(reactionGridWidth)
                        .align(Alignment.Center),
                    cells = GridCells.Fixed(columns)
                ) {
                    items(reactionCount) { index ->
                        itemContent(items[index])
                    }
                }
            }
        }
    }
}

/**
 * Preview of the [UserReactions] component with one user reaction.
 */
@Preview
@Composable
private fun OneUserReactionPreview() {
    ChatTheme {
        UserReactions(items = PreviewUserReactionData.oneUserReaction())
    }
}

/**
 * Preview of the [UserReactions] component with many user reactions.
 */
@Preview
@Composable
private fun ManyUserReactionsPreview() {
    ChatTheme {
        UserReactions(items = PreviewUserReactionData.manyUserReactions())
    }
}
