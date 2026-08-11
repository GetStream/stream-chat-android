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

package io.getstream.chat.android.client.extensions.internal

import io.getstream.chat.android.models.MemberInfo
import io.getstream.chat.android.randomMember
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class MemberExtensionsTests {

    @Test
    fun `toMemberInfo should narrow the member down to the slim shape`() {
        val member = randomMember(channelRole = "channel_moderator")
            .copy(notificationsMuted = true, extraData = mapOf("flair" to mapOf("tier" to "gold")))

        member.toMemberInfo() shouldBeEqualTo MemberInfo(
            channelRole = "channel_moderator",
            notificationsMuted = true,
            extraData = mapOf("flair" to mapOf("tier" to "gold")),
        )
    }

    @Test
    fun `toMemberInfo should treat an absent notificationsMuted as false`() {
        val member = randomMember().copy(notificationsMuted = null, extraData = emptyMap())

        member.toMemberInfo().notificationsMuted shouldBeEqualTo false
    }

    @Test
    fun `toMemberInfo should drop keys that are not member custom data`() {
        // user_id is not declared on the member DTO, so it reaches Member.extraData. The projection the backend puts
        // on message.member never carries it, so it must not leak into MemberInfo either.
        val member = randomMember().copy(
            extraData = mapOf("user_id" to "leandro", "flair" to mapOf("tier" to "gold")),
        )

        member.toMemberInfo().extraData shouldBeEqualTo mapOf("flair" to mapOf("tier" to "gold"))
    }
}
