/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.common.feature.messages.composer.mention

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class MentionTest {

    @Test
    fun `Given MentionType user when accessing value then return expected string`() {
        assertEquals("user", MentionType.user.value)
    }

    @Test
    fun `Given User mention when accessing type and display then return expected values`() {
        val user = io.getstream.chat.android.models.User(id = "user1", name = "John Doe")
        val mention = Mention.User(user)

        assertEquals(MentionType.user, mention.type)
        assertEquals("John Doe", mention.display)
    }
}
