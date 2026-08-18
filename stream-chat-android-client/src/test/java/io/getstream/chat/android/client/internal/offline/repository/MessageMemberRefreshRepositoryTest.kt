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

package io.getstream.chat.android.client.internal.offline.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.internal.offline.integration.BaseDomainTest2
import io.getstream.chat.android.models.MemberInfo
import io.getstream.chat.android.randomMessage
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Covers that refreshing the member snapshot reaches messages already held in the repository's in-memory cache.
 *
 * The cache is read before the database, so a targeted database update alone would leave stale objects being served.
 */
@RunWith(AndroidJUnit4::class)
internal class MessageMemberRefreshRepositoryTest : BaseDomainTest2() {

    private val member = MemberInfo(
        channelRole = "channel_moderator",
        notificationsMuted = true,
        extraData = mapOf("flair" to mapOf("tier" to "gold")),
    )

    @Test
    fun `refreshing the member updates a cached message`(): Unit = runTest {
        val message = cachedMessage()

        repos.updateChannelUserMessagesMember(message.cid, message.user.id, member)

        repos.selectMessage(message.id)?.member shouldBeEqualTo member
    }

    @Test
    fun `refreshing the member keeps the deprecated channelRole of a cached message in sync`(): Unit = runTest {
        val message = cachedMessage()

        repos.updateChannelUserMessagesMember(message.cid, message.user.id, member)

        @Suppress("DEPRECATION")
        repos.selectMessage(message.id)?.channelRole shouldBeEqualTo "channel_moderator"
    }

    @Test
    fun `clearing the member updates a cached message`(): Unit = runTest {
        val message = cachedMessage(member = member)

        repos.updateChannelUserMessagesMember(message.cid, message.user.id, null)

        repos.selectMessage(message.id)?.member.shouldBeNull()
    }

    @Test
    fun `refreshing the member leaves a cached message of another user alone`(): Unit = runTest {
        val message = cachedMessage()

        repos.updateChannelUserMessagesMember(message.cid, "someone-else", member)

        repos.selectMessage(message.id)?.member.shouldBeNull()
    }

    /** Inserting populates the repository cache synchronously, so the message is served from it afterwards. */
    private suspend fun cachedMessage(member: MemberInfo? = null) =
        randomMessage(user = data.user1, member = member, replyTo = null, poll = null)
            .also { repos.insertMessage(it) }
}
