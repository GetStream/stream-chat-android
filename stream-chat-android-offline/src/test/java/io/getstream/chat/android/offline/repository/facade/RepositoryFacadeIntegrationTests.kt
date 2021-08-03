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
import java.util.Date

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
            ),
            pinnedBy = randomUser(
                // ignoring fields that are not persisted on purpose
                totalUnreadCount = 0,
                unreadChannels = 0,
                online = false
            )
        )

        repositoryFacade.insertMessages(listOf(message), cache = false)
        val result = repositoryFacade.selectMessage(message.id)!!

        Truth.assertThat(result.id).isEqualTo(message.id)
        Truth.assertThat(result.cid).isEqualTo(message.cid)
        Truth.assertThat(result.text).isEqualTo(message.text)
        Truth.assertThat(result.html).isEqualTo(message.html)
        Truth.assertThat(result.parentId).isEqualTo(message.parentId)
        Truth.assertThat(result.command).isEqualTo(message.command)
        Truth.assertThat(result.attachments).isEqualTo(message.attachments)
        Truth.assertThat(result.mentionedUsersIds).isEqualTo(message.mentionedUsersIds)
        Truth.assertThat(result.mentionedUsers).isEqualTo(message.mentionedUsers)
        Truth.assertThat(result.replyCount).isEqualTo(message.replyCount)
        Truth.assertThat(result.reactionCounts).isEqualTo(message.reactionCounts)
        Truth.assertThat(result.reactionScores).isEqualTo(message.reactionScores)
        Truth.assertThat(result.syncStatus).isEqualTo(message.syncStatus)
        Truth.assertThat(result.type).isEqualTo(message.type)
        Truth.assertThat(result.latestReactions).isEqualTo(message.latestReactions)
        Truth.assertThat(result.ownReactions).isEqualTo(message.ownReactions)
        Truth.assertThat(result.createdAt).isEqualTo(message.createdAt)
        Truth.assertThat(result.updatedAt).isEqualTo(message.updatedAt)
        Truth.assertThat(result.deletedAt).isEqualTo(message.deletedAt)
        Truth.assertThat(result.updatedLocallyAt).isEqualTo(message.updatedLocallyAt)
        Truth.assertThat(result.createdLocallyAt).isEqualTo(message.createdLocallyAt)
        Truth.assertThat(result.user).isEqualTo(message.user)
        Truth.assertThat(result.extraData).isEqualTo(message.extraData)
        Truth.assertThat(result.silent).isEqualTo(message.silent)
        Truth.assertThat(result.shadowed).isEqualTo(message.shadowed)
        Truth.assertThat(result.i18n).isEqualTo(message.i18n)
        Truth.assertThat(result.showInChannel).isEqualTo(message.showInChannel)
        // Truth.assertThat(result.channelInfo).isEqualTo(message.channelInfo)
        Truth.assertThat(result.replyTo).isEqualTo(message.replyTo)
        Truth.assertThat(result.replyMessageId).isEqualTo(message.replyMessageId)
        Truth.assertThat(result.pinned).isEqualTo(message.pinned)
        Truth.assertThat(result.pinnedAt).isEqualTo(message.pinnedAt)
        Truth.assertThat(result.pinExpires).isEqualTo(message.pinExpires)
        Truth.assertThat(result.pinnedBy).isEqualTo(message.pinnedBy)
        Truth.assertThat(result.threadParticipants).isEqualTo(message.threadParticipants)

        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result).isEqualTo(message)
    }

    @Test
    fun `Given a message with theirs reaction When querying message Should return massage without own reactions`() =
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

            Truth.assertThat(result).isNotNull()
            Truth.assertThat(result!!.latestReactions).isEqualTo(mutableListOf(theirsReaction))
            Truth.assertThat(result!!.ownReactions).isEmpty()
        }

    @Test
    fun `Given a message with deleted own reaction When querying message Should return massage without own reactions`() =
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

            Truth.assertThat(result).isNotNull()
            Truth.assertThat(result!!.latestReactions).isEmpty()
            Truth.assertThat(result!!.ownReactions).isEmpty()
        }
}
