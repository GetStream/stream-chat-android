package io.getstream.chat.android.compose.previewdata

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.util.DefaultReactionTypes

/**
 * Provides sample reactions that will be used to render component previews.
 */
internal object PreviewReactionData {

    /**
     * A list of reaction drawable to
     * Drawable resource identifier won
     */

    val oneReaction: List<Pair<Int, Boolean>> = listOf(
        R.drawable.stream_compose_ic_reaction_thumbs_up to true
    )

    val manyReactions: List<Pair<Int, Boolean>> = listOf(
        R.drawable.stream_compose_ic_reaction_thumbs_up to true,
        R.drawable.stream_compose_ic_reaction_love to false,
        R.drawable.stream_compose_ic_reaction_wut to true,
        R.drawable.stream_compose_ic_reaction_thumbs_down to false
    )

    /**
     * A collection of messages with different sets of reactions. Possible reaction type values
     * are listed in [DefaultReactionTypes.defaultReactionTypes].
     */

    val messageWithOneReaction = Message().apply {
        latestReactions = mutableListOf(
            Reaction(type = "like"),
        )
        ownReactions = mutableListOf(
            Reaction(type = "like"),
        )
        reactionCounts = mutableMapOf(
            "like" to 1,
        )
    }

    val messageWithManyReactions = Message().apply {
        latestReactions = mutableListOf(
            Reaction(type = "like"),
            Reaction(type = "love"),
            Reaction(type = "wow"),
            Reaction(type = "sad"),
        )
        ownReactions = mutableListOf(
            Reaction(type = "like"),
            Reaction(type = "wow"),
        )
        reactionCounts = mutableMapOf(
            "like" to 1,
            "love" to 1,
            "wow" to 1,
            "sad" to 1
        )
    }
}
