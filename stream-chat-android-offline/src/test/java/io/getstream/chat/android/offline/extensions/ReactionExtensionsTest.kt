package io.getstream.chat.android.offline.extensions

import io.getstream.chat.android.offline.randomReaction
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should contain`
import org.junit.jupiter.api.Test

internal class ReactionExtensionsTest {

    @Test
    fun `Should merge correctly`() {
        val reaction1 = randomReaction(type = "type1")
        val reaction1Update = randomReaction(type = "type1")
        val reaction2 = randomReaction(type = "type2")

        val mergedResult = mergeReactions(listOf(reaction1Update), listOf(reaction1, reaction2))

        mergedResult.size `should be equal to` 2
        mergedResult `should contain` reaction1Update
        mergedResult `should contain` reaction2
    }
}
