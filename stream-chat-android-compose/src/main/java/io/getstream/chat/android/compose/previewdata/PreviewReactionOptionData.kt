package io.getstream.chat.android.compose.previewdata

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.reactionoptions.ReactionOptionItemState

/**
 * Provides sample reactions that will be used to render component previews.
 */
internal object PreviewReactionOptionData {

    @Composable
    fun reactionOption1() = ReactionOptionItemState(
        painter = painterResource(R.drawable.stream_compose_ic_reaction_thumbs_up),
        isSelected = true,
        type = "like",
    )

    @Composable
    fun reactionOption2() = ReactionOptionItemState(
        painter = painterResource(R.drawable.stream_compose_ic_reaction_love),
        isSelected = true,
        type = "love"
    )

    @Composable
    fun reactionOption3() = ReactionOptionItemState(
        painter = painterResource(R.drawable.stream_compose_ic_reaction_wut),
        isSelected = true,
        type = "wow"
    )

    @Composable
    fun reactionOption4() = ReactionOptionItemState(
        painter = painterResource(R.drawable.stream_compose_ic_reaction_thumbs_down),
        isSelected = true,
        type = "sad"
    )

    @Composable
    fun oneReaction(): List<ReactionOptionItemState> = listOf(
        reactionOption1()
    )

    @Composable
    fun manyReactions(): List<ReactionOptionItemState> = listOf(
        reactionOption1(),
        reactionOption2(),
        reactionOption3(),
        reactionOption4(),
    )
}
