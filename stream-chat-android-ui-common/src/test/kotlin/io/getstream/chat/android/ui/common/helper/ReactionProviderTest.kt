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

package io.getstream.chat.android.ui.common.helper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class ReactionProviderTest {

    @Test
    fun `test defaultProvider provides correct emojis`() {
        val provider = ReactionProvider.defaultProvider()
        assertEquals("👍", provider.emojiCode(ReactionDefaults.THUMBS_UP))
        assertEquals("❤️", provider.emojiCode(ReactionDefaults.LOVE))
        assertEquals("😂", provider.emojiCode(ReactionDefaults.LOL))
        assertEquals("😮", provider.emojiCode(ReactionDefaults.WUT))
        assertEquals("👎", provider.emojiCode(ReactionDefaults.THUMBS_DOWN))
        assertNull(provider.emojiCode("unknown_reaction"))
    }

    @Test
    fun `test defaultProvider returns emoji reaction type as emojiCode`() {
        val provider = ReactionProvider.defaultProvider()
        assertEquals("🎉", provider.emojiCode("🎉"))
    }

    @Test
    fun `test defaultProvider returns null for non-emoji non-default reaction`() {
        val provider = ReactionProvider.defaultProvider()
        assertNull(provider.emojiCode("custom_reaction"))
    }

    @Test
    fun `test defaultProvider availableReactions returns the 5 defaults`() {
        val provider = ReactionProvider.defaultProvider()
        val reactions = provider.availableReactions
        assertEquals(5, reactions.size)
        assertEquals("👍", reactions[ReactionDefaults.THUMBS_UP])
        assertEquals("❤️", reactions[ReactionDefaults.LOVE])
        assertEquals("😂", reactions[ReactionDefaults.LOL])
        assertEquals("😮", reactions[ReactionDefaults.WUT])
        assertEquals("👎", reactions[ReactionDefaults.THUMBS_DOWN])
    }
}
