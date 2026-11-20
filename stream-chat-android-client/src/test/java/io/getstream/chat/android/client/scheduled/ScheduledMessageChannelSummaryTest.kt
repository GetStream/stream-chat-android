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

package io.getstream.chat.android.client.scheduled

import io.getstream.chat.android.models.ChannelInfo
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomScheduledMessage
import io.getstream.chat.android.randomString
import io.getstream.result.Result
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeEqualTo
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.util.Date

/**
 * The summary of the channel of a scheduled message: from the channel saved on this device until the server sends
 * one, then the last one the server sent.
 */
internal class ScheduledMessageChannelSummaryTest {

    private val fixture = ScheduledMessageManagerFixture()
    private val manager = fixture.manager
    private val repositoryFacade = fixture.repositoryFacade
    private val cid = randomCID()
    private val later = Date(fixture.now.time + 3_600_000)

    @Test
    fun `a message scheduled on this device takes the summary of its channel from the saved channel`() = runTest {
        startOffline()
        val members = (5 downTo 1).map { day -> randomMember(createdAt = Date(day * 86_400_000L)) }
        val (type, id) = cid.split(":")
        val channel = randomChannel(id = id, type = type, name = "", memberCount = 5, members = members)
            .copy(image = "https://example.com/team.png")
        whenever(repositoryFacade.selectChannel(cid)) doReturn channel

        val scheduled = (manager.schedule(cid, Message(id = randomString()), later) as Result.Success).value

        scheduled.channel shouldBeEqualTo ChannelInfo(
            cid = cid,
            id = id,
            type = type,
            memberCount = 5,
            name = null,
            image = "https://example.com/team.png",
            members = members.reversed().take(4),
        )
    }

    @Test
    fun `a message scheduled on this device has no summary when its channel is not saved`() = runTest {
        startOffline()

        val scheduled = (manager.schedule(cid, Message(id = randomString()), later) as Result.Success).value

        scheduled.channel shouldBeEqualTo null
    }

    @Test
    fun `the summary the server sends replaces the one from the saved channel`() = runTest {
        startOffline()
        whenever(repositoryFacade.selectChannel(cid)) doReturn randomChannel(id = cid.substringAfter(":"))
        val scheduled = (manager.schedule(cid, Message(id = randomString()), later) as Result.Success).value
        val summary = ChannelInfo(cid = cid, memberCount = 2, members = listOf(randomMember(), randomMember()))
        scheduled.channel shouldNotBeEqualTo null

        manager.onEvent(scheduledMessageCreatedEvent(scheduled.copy(channel = summary, updatedAt = later)))

        manager.scheduledMessages.value.single().channel shouldBeEqualTo summary
    }

    @Test
    fun `an event without the summary keeps the one known`() = runTest {
        val summary = ChannelInfo(cid = cid, memberCount = 2, members = listOf(randomMember(), randomMember()))
        val known = randomScheduledMessage(cid = cid, updatedAt = fixture.now).copy(channel = summary)
        manager.onEvent(scheduledMessageCreatedEvent(known))

        manager.onEvent(scheduledMessageCreatedEvent(known.copy(channel = null, updatedAt = later)))

        manager.scheduledMessages.value.single().channel shouldBeEqualTo summary
    }

    /** Starts saving to the repository, offline so nothing is sent. */
    private suspend fun TestScope.startOffline() {
        fixture.connectionState.value = ConnectionState.Offline
        manager.start(backgroundScope)
    }
}
