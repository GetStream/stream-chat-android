/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.models

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.User
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

internal class ExtensionsTests {

    @Test
    fun noUnread() {
        Channel().getUnreadMessagesCount() shouldBeEqualTo 0
    }

    @Test
    fun totalUnread() {
        Channel(read = listOf(getRead(10))).getUnreadMessagesCount() shouldBeEqualTo 10

        Channel(
            read = listOf(
                getRead(10),
                getRead(10)
            )
        ).getUnreadMessagesCount() shouldBeEqualTo 20
    }

    @Test
    fun unreadByUsers() {

        val userA = "user-a"
        val userB = "user-b"
        val unreadUserA = 10
        val unreadUserB = 5

        val channel = Channel(
            read = listOf(
                getRead(unreadUserA, userA),
                getRead(unreadUserB, userB)
            )
        )

        channel.getUnreadMessagesCount() shouldBeEqualTo unreadUserA + unreadUserB
        channel.getUnreadMessagesCount(userA) shouldBeEqualTo unreadUserA
        channel.getUnreadMessagesCount(userB) shouldBeEqualTo unreadUserB
    }

    private fun getRead(unreadCount: Int, userId: String = ""): ChannelUserRead {
        return ChannelUserRead(User(id = userId), null, unreadCount)
    }
}
