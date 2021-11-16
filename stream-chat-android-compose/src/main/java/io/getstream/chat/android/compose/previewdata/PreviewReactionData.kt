package io.getstream.chat.android.compose.previewdata

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.reaction.ReactionOption

/**
 * Provides sample reactions that will be used to render component previews.
 */
internal object PreviewReactionData {

    @Composable
    fun oneReaction(): List<ReactionOption> = listOf(
        ReactionOption(
            painter = painterResource(R.drawable.stream_compose_ic_reaction_thumbs_up),
            isSelected = true,
            type = "like",
        ),
    )

    @Composable
    fun manyReactions(): List<ReactionOption> = listOf(
        ReactionOption(
            painter = painterResource(R.drawable.stream_compose_ic_reaction_thumbs_up),
            isSelected = true,
            type = "like"
        ),
        ReactionOption(
            painter = painterResource(R.drawable.stream_compose_ic_reaction_love),
            isSelected = true,
            type = "love"
        ),
        ReactionOption(
            painter = painterResource(R.drawable.stream_compose_ic_reaction_wut),
            isSelected = true,
            type = "wow"
        ),
        ReactionOption(
            painter = painterResource(R.drawable.stream_compose_ic_reaction_thumbs_down),
            isSelected = true,
            type = "sad"
        ),
    )
}
