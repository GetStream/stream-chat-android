package io.getstream.chat.android.compose.previewdata

import io.getstream.chat.android.client.models.Reaction

/**
 * Provides sample reactions that will be used to render component previews.
 */
internal object PreviewReactionData {

    private val reaction1: Reaction = Reaction(
        type = "like",
        user = PreviewUserData.user1
    )

    private val reaction2: Reaction = Reaction(
        type = "love",
        user = PreviewUserData.user2
    )

    private val reaction3: Reaction = Reaction(
        type = "wow",
        user = PreviewUserData.user3
    )

    private val reaction4: Reaction = Reaction(
        type = "sad",
        user = PreviewUserData.user4
    )

    val oneReaction: List<Reaction> = listOf(reaction1)

    val manyReaction: List<Reaction> = listOf(reaction1, reaction2, reaction3, reaction4)
}
