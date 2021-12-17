package io.getstream.chat.android.compose.previewdata

import io.getstream.chat.android.client.models.Reaction

/**
 * Provides sample reaction that will be used to render component previews.
 */
@Suppress("MemberVisibilityCanBePrivate")
internal object PreviewReactionData {

    val reaction1: Reaction = Reaction(
        type = "like",
        user = PreviewUserData.user1
    )

    val reaction2: Reaction = Reaction(
        type = "love",
        user = PreviewUserData.user2
    )

    val reaction3: Reaction = Reaction(
        type = "wow",
        user = PreviewUserData.user3
    )

    val reaction4: Reaction = Reaction(
        type = "sad",
        user = PreviewUserData.user4
    )

    val oneReaction: List<Reaction> = listOf(reaction1)

    val manyReaction: List<Reaction> = listOf(reaction1, reaction2, reaction3, reaction4)
}
