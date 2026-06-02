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

package io.getstream.chat.android.compose.ui.util

import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class ChannelUtilsTest {

    @Test
    fun `Given a DM with current user and counterpart Should return the counterpart id`() {
        val currentUser = randomUser()
        val counterpart = randomUser()
        val channel = dmChannel(currentUser, counterpart)

        val id = channel.dmCounterpartId(currentUser)

        assertEquals(counterpart.id, id)
    }

    @Test
    fun `Given a non-distinct channel Should return null`() {
        val currentUser = randomUser()
        val counterpart = randomUser()
        val channel = randomChannel(
            id = "regular-id",
            type = "messaging",
            members = listOf(randomMember(user = currentUser), randomMember(user = counterpart)),
        )

        assertNull(channel.dmCounterpartId(currentUser))
    }

    @Test
    fun `Given a distinct channel with three members Should return null`() {
        val currentUser = randomUser()
        val channel = randomChannel(
            id = "!members-${currentUser.id}-a-b",
            type = "messaging",
            members = listOf(
                randomMember(user = currentUser),
                randomMember(user = randomUser()),
                randomMember(user = randomUser()),
            ),
        )

        assertNull(channel.dmCounterpartId(currentUser))
    }

    @Test
    fun `Given a distinct two-member channel without the current user Should return null`() {
        val currentUser = randomUser()
        val channel = randomChannel(
            id = "!members-a-b",
            type = "messaging",
            members = listOf(randomMember(user = randomUser()), randomMember(user = randomUser())),
        )

        assertNull(channel.dmCounterpartId(currentUser))
    }

    @Test
    fun `Given a null current user Should return null`() {
        val channel = dmChannel(randomUser(), randomUser())

        assertNull(channel.dmCounterpartId(currentUser = null))
    }

    private fun dmChannel(currentUser: User, counterpart: User) = randomChannel(
        id = "!members-${currentUser.id}-${counterpart.id}",
        type = "messaging",
        members = listOf(randomMember(user = currentUser), randomMember(user = counterpart)),
    )
}
