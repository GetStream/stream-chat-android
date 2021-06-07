package io.getstream.chat.android.offline.repository.facade

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.offline.integration.BaseRepositoryFacadeIntegrationTest
import io.getstream.chat.android.offline.randomMessage
import io.getstream.chat.android.offline.randomReaction
import io.getstream.chat.android.offline.randomUser
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class RepositoryFacadeIntegrationTests : BaseRepositoryFacadeIntegrationTest() {

    @Test
    fun `Given a message in the database When persisting the updated message Should store the update`() = runBlocking {
        val id = randomString()
        val originalMessage = randomMessage(id = id)
        val updatedText = randomString()
        val updatedMessage = originalMessage.copy(text = updatedText)

        repositoryFacade.insertMessages(listOf(originalMessage), cache = false)
        repositoryFacade.insertMessages(listOf(updatedMessage), cache = false)
        val result = repositoryFacade.selectMessage(id)

        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result!!.text).isEqualTo(updatedText)
    }

    @Test
    fun `Given a message When persisting the message Should store required fields`() = runBlocking {
        val message = randomMessage(
            user = randomUser(
                // ignoring fields that are not persisted on purpose
                totalUnreadCount = 0,
                unreadChannels = 0,
                online = false
            )
        )

        repositoryFacade.insertMessages(listOf(message), cache = false)
        val result = repositoryFacade.selectMessage(message.id)

        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result).isEqualTo(message)
    }

    @Test
    fun `Given a message with theirs reaction When querying message Should return massage without own reactions`() =
        runBlocking {
            val messageId = randomString()
            val user = randomUser(
                // ignoring fields that are not persisted on purpose
                totalUnreadCount = 0,
                unreadChannels = 0,
                online = false
            )
            val theirsReaction = randomReaction(
                messageId = messageId,
                type = "haha",
                user = user,
                userId = user.id
            )
            val message = randomMessage(
                id = messageId,
                ownReactions = mutableListOf(),
                latestReactions = mutableListOf(theirsReaction),
                reactionCounts = mutableMapOf("haha" to 1),
                reactionScores = mutableMapOf("haha" to 1),
            )

            repositoryFacade.insertMessages(listOf(message), cache = false)
            val result = repositoryFacade.selectMessage(message.id)

            Truth.assertThat(result).isNotNull()
            Truth.assertThat(result!!.latestReactions).isEqualTo(mutableListOf(theirsReaction))
            Truth.assertThat(result!!.ownReactions).isEmpty()
            Truth.assertThat(result!!.reactionCounts.size).isEqualTo(1)
            Truth.assertThat(result!!.reactionCounts["haha"]).isEqualTo(1)
            Truth.assertThat(result!!.reactionScores.size).isEqualTo(1)
            Truth.assertThat(result!!.reactionScores["haha"]).isEqualTo(1)
        }
}
