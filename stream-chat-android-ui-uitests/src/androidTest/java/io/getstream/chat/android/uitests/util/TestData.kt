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

package io.getstream.chat.android.uitests.util

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageType
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

/**
 * Provides sample users for UI tests.
 */
object TestData {

    fun user1(): User = User().copy(
        id = "jc",
        name = "Jc Miñarro",
        image = FakeImageLoader.AVATAR_JC,
        online = true,
    )

    fun user2(): User = User().copy(
        id = "leia_organa",
        name = "Leia Organa",
        image = FakeImageLoader.AVATAR_LEIA,
    )

    fun user3(): User = User().copy(
        id = "chewbacca",
        name = "Chewbacca",
        image = FakeImageLoader.AVATAR_CHEWBACCA,
    )

    fun user4(): User = User().copy(
        id = "anakin_skywalker",
        name = "Anakin Skywalker",
        image = FakeImageLoader.AVATAR_ANAKIN,
    )

    fun user5(): User = User().copy(
        id = "han_solo",
        name = "Han Solo",
        image = FakeImageLoader.AVATAR_HAN,
    )

    fun user6(): User = User().copy(
        id = "jake",
        image = FakeImageLoader.AVATAR_LEIA,
    )

    fun member1() = Member(
        user = user1(),
        isInvited = false,
    )

    fun member2() = Member(
        user = user2(),
        isInvited = false,
    )

    fun member3() = Member(
        user = user3(),
        isInvited = false,
    )

    fun member4() = Member(
        user = user4(),
        isInvited = false,
    )

    fun member5() = Member(
        user = user5(),
        isInvited = false,
    )

    fun message1(): Message = Message().copy(
        id = "message1",
        text = "Ladies and gentlemen, we have liftoff",
        createdAt = date1(),
        type = MessageType.REGULAR,
        user = user1(),
    )

    fun message2(): Message = Message().copy(
        id = "message2",
        text = "Space!",
        createdAt = date2(),
        type = MessageType.REGULAR,
        user = user2(),
    )

    fun message3(): Message = Message().copy(
        id = "message3",
        text = "They ain't in space yet",
        createdAt = date3(),
        type = MessageType.REGULAR,
        user = user3(),
    )

    fun message4(): Message = Message().copy(
        id = "message4",
        text = "OK, not to be that guy, but by my calculations, they’re technically in space now",
        createdAt = date4(),
        type = MessageType.REGULAR,
        user = user4(),
    )

    fun message5(): Message = Message().copy(
        id = "message5",
        text = "tagged @Han Solo ",
        createdAt = date1(),
        type = MessageType.REGULAR,
        user = user4(),
        mentionedUsers = listOf(user5()),
    )

    fun message6(): Message = Message().copy(
        id = "message6",
        text = "tagged @jake ",
        createdAt = date1(),
        type = MessageType.REGULAR,
        user = user5(),
        mentionedUsers = listOf(user6()),
    )

    fun channel1() = Channel().copy(
        type = "messaging",
        id = "channel1",
        unreadCount = 0,
    )

    fun channel2() = Channel().copy(
        type = "messaging",
        id = "channel2",
        unreadCount = 0,
    )

    fun channel3() = Channel().copy(
        type = "messaging",
        id = "channel3",
        unreadCount = 0,
    )

    fun channel4() = Channel().copy(
        type = "messaging",
        id = "channel4",
        unreadCount = 0,
    )

    fun date1() = december2020At9(day = 7)

    fun date2() = december2020At9(day = 8)

    fun date3() = december2020At9(day = 9)

    fun date4() = december2020At9(day = 10)

    fun reaction1(): Reaction = Reaction(
        type = "like",
        user = user1(),
    )

    fun reaction2(): Reaction = Reaction(
        type = "love",
        user = user2(),
    )

    fun reaction3(): Reaction = Reaction(
        type = "wow",
        user = user3(),
    )

    fun reaction4(): Reaction = Reaction(
        type = "sad",
        user = user4(),
    )

    private fun december2020At9(day: Int): Date =
        GregorianCalendar(2020, Calendar.DECEMBER, day, 9, 0).time
}
