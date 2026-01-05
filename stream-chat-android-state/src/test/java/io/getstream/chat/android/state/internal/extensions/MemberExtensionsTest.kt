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

import io.getstream.chat.android.client.extensions.internal.updateUsers
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomUser
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class MemberExtensionsTest {

    @Test
    fun `Should update members correctly`() {
        val user1 = randomUser(
            id = "userId1",
            name = "userName1",
        )
        val member1 = randomMember(user = user1)
        val member2 = randomMember()
        val user1Updated = randomUser(
            id = "userId1",
            name = "userName2",
        )

        val result = listOf(member1, member2).updateUsers(mapOf(user1Updated.id to user1Updated))

        result.any { it.user == user1 } shouldBeEqualTo false
        result.any { it.user == user1Updated } shouldBeEqualTo true
    }
}
