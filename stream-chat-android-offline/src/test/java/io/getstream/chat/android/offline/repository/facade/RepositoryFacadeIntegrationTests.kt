package io.getstream.chat.android.offline.repository.facade

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.offline.integration.BaseRepositoryFacadeIntegrationTest
import io.getstream.chat.android.offline.randomChannelInfo
import io.getstream.chat.android.offline.randomMessage
import io.getstream.chat.android.offline.randomReaction
import io.getstream.chat.android.offline.randomUser
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.runBlocking
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
        runBlocking {
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
    fun `Given a message When persisting the message Should store required fields`(): Unit = runBlocking {
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
        runBlocking {
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
            val result = repositoryFacade.selectMessage(message.id)

            result.shouldNotBeNull()
            result.latestReactions shouldBeEqualTo mutableListOf(theirsReaction)
            result.ownReactions.shouldBeEmpty()
        }

    @Test
    fun `Given a message with deleted own reaction When querying message Should return massage without own reactions`(): Unit =
        runBlocking {
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
        runBlocking {
            val message = randomMessage(channelInfo = null)

            repositoryFacade.insertMessages(listOf(message), cache = false)
            val result = repositoryFacade.selectMessage(message.id)

            result?.channelInfo.shouldBeNull()
        }

    @Test
    fun `Given a message with channel info When querying message Should return message with the same channel info`(): Unit =
        runBlocking {
            val channelInfo = randomChannelInfo()
            val message = randomMessage(channelInfo = channelInfo)

            repositoryFacade.insertMessages(listOf(message), cache = false)
            val result = repositoryFacade.selectMessage(message.id)

            result?.channelInfo shouldBeEqualTo channelInfo
        }
}
