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

package io.getstream.chat.android.offline.repository.facade

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.test.randomChannelInfo
import io.getstream.chat.android.client.test.randomMessage
import io.getstream.chat.android.client.test.randomReaction
import io.getstream.chat.android.client.test.randomUser
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.offline.integration.BaseRepositoryFacadeIntegrationTest
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeNull
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
internal class RepositoryFacadeIntegrationTests : BaseRepositoryFacadeIntegrationTest() {

    @Test
    fun `Given a message in the database When persisting the updated message Should store the update`(): Unit =
        runTest {
            val id = randomString()
            val originalMessage = randomMessage(id = id)
            val updatedText = randomString()
            val updatedMessage = originalMessage.copy(text = updatedText)

            repositoryFacade.insertMessages(listOf(originalMessage), cache = false)
            repositoryFacade.insertMessages(listOf(updatedMessage), cache = false)
            val result = repositoryFacade.selectMessage(id)

            result.shouldNotBeNull()
            result.text shouldBeEqualTo updatedText
        }

    @Test
    fun `Given a message When persisting the message Should store required fields`(): Unit = runTest {
        val message = randomMessage(
            user = randomUser(
                // ignoring fields that are not persisted on purpose
                totalUnreadCount = 0,
                unreadChannels = 0,
                online = false
            ),
            pinnedBy = randomUser(
                // ignoring fields that are not persisted on purpose
                totalUnreadCount = 0,
                unreadChannels = 0,
                online = false
            )
        )

        repositoryFacade.insertMessages(listOf(message), cache = false)
        val result = repositoryFacade.selectMessage(message.id)

        result.shouldNotBeNull()
        result shouldBeEqualTo message
    }

    @Test
    fun `Given a message with theirs reaction When querying message Should return massage without own reactions`(): Unit =
        runTest {
            val messageId = randomString()
            val theirsUser = randomUser(
                // ignoring fields that are not persisted on purpose
                totalUnreadCount = 0,
                unreadChannels = 0,
                online = false
            )
            val theirsReaction = randomReaction(
                messageId = messageId,
                user = theirsUser,
                userId = theirsUser.id,
                deletedAt = null

            )
            val message = randomMessage(
                id = messageId,
                ownReactions = mutableListOf(),
                latestReactions = mutableListOf(theirsReaction),
            )

            repositoryFacade.insertCurrentUser(randomUser())
            repositoryFacade.insertMessages(listOf(message), cache = false)
            val result: Message? = repositoryFacade.selectMessage(message.id)

            result.shouldNotBeNull()
            result.latestReactions shouldBeEqualTo mutableListOf(theirsReaction)
            result.ownReactions.shouldBeEmpty()
        }

    @Test
    fun `Given a message with deleted own reaction When querying message Should return massage without own reactions`(): Unit =
        runTest {
            val messageId = randomString()
            val mineDeletedReaction = randomReaction(
                messageId = messageId,
                user = currentUser,
                userId = currentUser.id,
                deletedAt = Date()

            )
            val message = randomMessage(
                id = messageId,
                ownReactions = mutableListOf(mineDeletedReaction),
                latestReactions = mutableListOf(mineDeletedReaction),
            )

            repositoryFacade.insertMessages(listOf(message), cache = false)
            val result = repositoryFacade.selectMessage(message.id)

            result.shouldNotBeNull()
            result.latestReactions.shouldBeEmpty()
            result.ownReactions.shouldBeEmpty()
        }

    @Test
    fun `Given a message without channel info When querying message Should return message with null channel info`() =
        runTest {
            val message = randomMessage(channelInfo = null)

            repositoryFacade.insertMessages(listOf(message), cache = false)
            val result = repositoryFacade.selectMessage(message.id)

            result?.channelInfo.shouldBeNull()
        }

    @Test
    fun `Given a message with channel info When querying message Should return message with the same channel info`(): Unit =
        runTest {
            val channelInfo = randomChannelInfo()
            val message = randomMessage(channelInfo = channelInfo)

            repositoryFacade.insertMessages(listOf(message), cache = false)
            val result = repositoryFacade.selectMessage(message.id)

            result?.channelInfo shouldBeEqualTo channelInfo
        }
}
