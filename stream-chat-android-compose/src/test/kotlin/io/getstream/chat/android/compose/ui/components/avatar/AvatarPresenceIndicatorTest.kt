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

package io.getstream.chat.android.compose.ui.components.avatar

import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

internal class AvatarPresenceIndicatorTest {

    @Test
    fun `online user resolves to Online regardless of showWhenOffline`() {
        val user = randomUser(online = true)

        user.avatarPresenceIndicator() shouldBeEqualTo AvatarPresenceIndicator.Online
        user.avatarPresenceIndicator(showWhenOffline = true) shouldBeEqualTo AvatarPresenceIndicator.Online
    }

    @Test
    fun `offline user resolves to None by default`() {
        val user = randomUser(online = false)

        user.avatarPresenceIndicator() shouldBeEqualTo AvatarPresenceIndicator.None
    }

    @Test
    fun `offline user resolves to Offline when showWhenOffline is true`() {
        val user = randomUser(online = false)

        user.avatarPresenceIndicator(showWhenOffline = true) shouldBeEqualTo AvatarPresenceIndicator.Offline
    }

    @Test
    fun `channel with an online member other than the current user resolves to Online`() {
        val currentUser = randomUser(id = "me", online = false)
        val channel = randomChannel(
            members = listOf(
                randomMember(user = currentUser),
                randomMember(user = randomUser(id = "other", online = true)),
            ),
        )

        channel.avatarPresenceIndicator(currentUser) shouldBeEqualTo AvatarPresenceIndicator.Online
    }

    @Test
    fun `channel where only the current user is online resolves to None by default`() {
        val currentUser = randomUser(id = "me", online = true)
        val channel = randomChannel(
            members = listOf(
                randomMember(user = currentUser),
                randomMember(user = randomUser(id = "other", online = false)),
            ),
        )

        channel.avatarPresenceIndicator(currentUser) shouldBeEqualTo AvatarPresenceIndicator.None
    }

    @Test
    fun `offline channel resolves to Offline when showWhenOffline is true`() {
        val currentUser = randomUser(id = "me", online = false)
        val channel = randomChannel(
            members = listOf(
                randomMember(user = currentUser),
                randomMember(user = randomUser(id = "other", online = false)),
            ),
        )

        channel.avatarPresenceIndicator(currentUser, showWhenOffline = true) shouldBeEqualTo
            AvatarPresenceIndicator.Offline
    }

    @Test
    fun `channel with a null current user resolves to Online when any member is online`() {
        val channel = randomChannel(
            members = listOf(
                randomMember(user = randomUser(id = randomString(), online = false)),
                randomMember(user = randomUser(id = randomString(), online = true)),
            ),
        )

        channel.avatarPresenceIndicator(currentUser = null) shouldBeEqualTo AvatarPresenceIndicator.Online
    }
}
