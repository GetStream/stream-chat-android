package io.getstream.chat.android.offline.repository.factory.internal

import io.getstream.chat.android.client.persistance.repository.AttachmentRepository
import io.getstream.chat.android.client.persistance.repository.ChannelConfigRepository
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.persistance.repository.QueryChannelsRepository
import io.getstream.chat.android.client.persistance.repository.ReactionRepository
import io.getstream.chat.android.client.persistance.repository.SyncStateRepository
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.offline.repository.database.internal.ChatDatabase
import io.getstream.chat.android.offline.repository.domain.channel.internal.ChannelDao
import io.getstream.chat.android.offline.repository.domain.channelconfig.internal.ChannelConfigDao
import io.getstream.chat.android.offline.repository.domain.message.attachment.internal.AttachmentDao
import io.getstream.chat.android.offline.repository.domain.message.internal.MessageDao
import io.getstream.chat.android.offline.repository.domain.queryChannels.internal.QueryChannelsDao
import io.getstream.chat.android.offline.repository.domain.reaction.internal.ReactionDao
import io.getstream.chat.android.offline.repository.domain.syncState.internal.SyncStateDao
import io.getstream.chat.android.offline.repository.domain.user.internal.UserDao
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class DatabaseRepositoryFactoryTest {

    private val attachmentDao: AttachmentDao = mock()
    private val userDao: UserDao = mock()
    private val channelConfigDao: ChannelConfigDao = mock()
    private val channelDao: ChannelDao = mock()
    private val queryChannelsDao: QueryChannelsDao = mock()
    private val messageDao: MessageDao = mock()
    private val reactionDao: ReactionDao = mock()
    private val syncStateDao: SyncStateDao = mock()

    private val database: ChatDatabase = mock {
        on(it.attachmentDao()) doReturn attachmentDao
        on(it.userDao()) doReturn userDao
        on(it.channelConfigDao()) doReturn channelConfigDao
        on(it.channelStateDao()) doReturn channelDao
        on(it.queryChannelsDao()) doReturn queryChannelsDao
        on(it.messageDao()) doReturn messageDao
        on(it.reactionDao()) doReturn reactionDao
        on(it.syncStateDao()) doReturn syncStateDao
    }
    private val databaseRepositoryFactory = DatabaseRepositoryFactory(database, mock())

    @Test
    fun `cache of repositories should work correctly for UserRepository`() {
        val repository = databaseRepositoryFactory.run {
            createUserRepository()
            get(UserRepository::class.java)
        }

        assertNotNull(repository)
    }

    @Test
    fun `cache of repositories should work correctly for ChannelConfigRepository`() {
        val repository = databaseRepositoryFactory.run {
            createChannelConfigRepository()
            get(ChannelConfigRepository::class.java)
        }

        assertNotNull(repository)
    }

    @Test
    fun `cache of repositories should work correctly for ChannelRepository`() {
        val repository = databaseRepositoryFactory.run {
            createChannelRepository(mock(), mock())
            get(ChannelRepository::class.java)
        }

        assertNotNull(repository)
    }

    @Test
    fun `cache of repositories should work correctly for QueryChannelsRepository`() {
        val repository = databaseRepositoryFactory.run {
            createQueryChannelsRepository()
            get(QueryChannelsRepository::class.java)
        }

        assertNotNull(repository)
    }

    @Test
    fun `cache of repositories should work correctly for MessageRepository`() {
        val repository = databaseRepositoryFactory.run {
            createMessageRepository(mock())
            get(MessageRepository::class.java)
        }

        assertNotNull(repository)
    }

    @Test
    fun `cache of repositories should work correctly for ReactionRepository`() {
        val repository = databaseRepositoryFactory.run {
            createReactionRepository(mock())
            get(ReactionRepository::class.java)
        }

        assertNotNull(repository)
    }

    @Test
    fun `cache of repositories should work correctly for SyncStateRepository`() {
        val repository = databaseRepositoryFactory.run {
            createSyncStateRepository()
            get(SyncStateRepository::class.java)
        }

        assertNotNull(repository)
    }

    @Test
    fun `cache of repositories should work correctly for AttachmentRepository`() {
        val repository = databaseRepositoryFactory.run {
            createAttachmentRepository()
            get(AttachmentRepository::class.java)
        }

        assertNotNull(repository)
    }
}
