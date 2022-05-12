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

package io.getstream.chat.android.uitests.util

import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import java.util.Date

/**
 * Provides sample users for UI tests.
 */
@OptIn(InternalStreamChatApi::class)
object TestData {

    fun user1(): User = User().apply {
        id = "jc"
        name = "Jc Miñarro"
        image = FakeImageLoader.AVATAR_JC
        online = true
    }

    fun user2(): User = User().apply {
        id = "amit"
        name = "Amit Kumar"
        image = FakeImageLoader.AVATAR_AMIT
    }

    fun user3(): User = User().apply {
        id = "filip"
        name = "Filip Babić"
        image = FakeImageLoader.AVATAR_FILIP
    }

    fun user4(): User = User().apply {
        id = "rafal"
        name = "Rafal Adasiewicz"
        image = FakeImageLoader.AVATAR_RAFAL
    }

    fun user5(): User = User().apply {
        id = "belal"
        name = "Belal Khan"
        image = FakeImageLoader.AVATAR_BELAL
    }

    fun member1() = Member(
        user = user1(),
        role = "user",
        isInvited = false
    )

    fun member2() = Member(
        user = user2(),
        role = "user",
        isInvited = false
    )

    fun member3() = Member(
        user = user3(),
        role = "user",
        isInvited = false
    )

    fun member4() = Member(
        user = user4(),
        role = "user",
        isInvited = false
    )

    fun member5() = Member(
        user = user5(),
        role = "user",
        isInvited = false
    )

    fun message1(): Message = Message().apply {
        id = "message1"
        text = "Ladies and gentlemen, we have liftoff"
        createdAt = date1()
        type = ModelType.message_regular
        user = user1()
    }

    fun message2(): Message = Message().apply {
        id = "message2"
        text = "Space!"
        createdAt = date2()
        type = ModelType.message_regular
        user = user2()
    }

    fun message3(): Message = Message().apply {
        id = "message3"
        text = "They ain't in space yet"
        createdAt = date3()
        type = ModelType.message_regular
        user = user3()
    }

    fun message4(): Message = Message().apply {
        id = "message4"
        text = "OK, not to be that guy, but by my calculations, they’re technically in space now"
        createdAt = date4()
        type = ModelType.message_regular
        user = user4()
    }

    fun channel1() = Channel().apply {
        type = "messaging"
        id = "channel1"
        cid = "messaging:channel1"
        unreadCount = 0
    }

    fun channel2() = Channel().apply {
        type = "messaging"
        id = "channel2"
        cid = "messaging:channel2"
        unreadCount = 0
    }

    fun channel3() = Channel().apply {
        type = "messaging"
        id = "channel3"
        cid = "messaging:channel3"
        unreadCount = 0
    }

    fun channel4() = Channel().apply {
        type = "messaging"
        id = "channel4"
        cid = "messaging:channel4"
        unreadCount = 0
    }

    fun date1() = LocalDateTime.of(2020, 12, 7, 9, 0).toDate()

    fun date2() = LocalDateTime.of(2020, 12, 8, 9, 0).toDate()

    fun date3() = LocalDateTime.of(2020, 12, 9, 9, 0).toDate()

    fun date4() = LocalDateTime.of(2020, 12, 10, 9, 0).toDate()

    private fun LocalDateTime.toDate(): Date {
        return Date(
            atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
    }
}
