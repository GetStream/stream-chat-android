package io.getstream.chat.android.compose.ui.components.userreactions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
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
 * @param maxColumns The maximum number of columns in the user reactions grid.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun UserReactions(
    items: List<UserReactionItemState>,
    modifier: Modifier = Modifier,
    maxColumns: Int = 4,
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

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = ChatTheme.dimens.userReactionsMaxHeight),
        shape = RoundedCornerShape(16.dp),
        color = ChatTheme.colors.barsBackground,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Text(
                text = reactionCountText,
                style = ChatTheme.typography.title3Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = ChatTheme.colors.textHighEmphasis
            )

            Spacer(modifier = Modifier.height(16.dp))

            val columns = reactionCount.coerceAtMost(maxColumns)
            val reactionItemWidth = 80.dp
            val reactionGridWidth = reactionItemWidth * columns

            LazyVerticalGrid(
                modifier = Modifier.width(reactionGridWidth),
                cells = GridCells.Fixed(reactionCount.coerceAtMost(columns))
            ) {
                items(reactionCount) { index ->
                    itemContent(items[index])
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
