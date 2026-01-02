/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.state.internal.extensions

import io.getstream.chat.android.client.extensions.internal.mergeReactions
import io.getstream.chat.android.randomReaction
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
