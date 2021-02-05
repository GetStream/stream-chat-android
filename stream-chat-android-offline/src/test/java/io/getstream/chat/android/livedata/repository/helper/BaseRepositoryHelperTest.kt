package io.getstream.chat.android.livedata.repository.helper

import androidx.annotation.CallSuper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.livedata.repository.ChannelConfigRepository
import io.getstream.chat.android.livedata.repository.ChannelRepository
import io.getstream.chat.android.livedata.repository.MessageRepository
import io.getstream.chat.android.livedata.repository.QueryChannelsRepository
import io.getstream.chat.android.livedata.repository.ReactionRepository
import io.getstream.chat.android.livedata.repository.RepositoryFactory
import io.getstream.chat.android.livedata.repository.RepositoryHelper
import io.getstream.chat.android.livedata.repository.SyncStateRepository
import io.getstream.chat.android.livedata.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.BeforeEach

@ExperimentalCoroutinesApi
internal open class BaseRepositoryHelperTest {

    protected lateinit var users: UserRepository
    protected lateinit var configs: ChannelConfigRepository
    protected lateinit var channels: ChannelRepository
    protected lateinit var queryChannels: QueryChannelsRepository
    protected lateinit var messages: MessageRepository
    protected lateinit var reactions: ReactionRepository
    protected lateinit var syncState: SyncStateRepository

    protected val scope = TestCoroutineScope()

    protected lateinit var sut: RepositoryHelper

    @CallSuper
    @BeforeEach
    fun setUp() {
        users = mock()
        configs = mock()
        channels = mock()
        queryChannels = mock()
        messages = mock()
        reactions = mock()
        syncState = mock()
        val factory: RepositoryFactory = mock {
            on { createUserRepository() } doReturn users
            on { createChannelConfigRepository() } doReturn configs
            on { createChannelRepository(any(), any()) } doReturn channels
            on { createQueryChannelsRepository() } doReturn queryChannels
            on { createMessageRepository(any()) } doReturn messages
            on { createReactionRepository() } doReturn reactions
            on { createSyncStateRepository() } doReturn syncState
        }
        sut = RepositoryHelper.create(factory, scope, mock())
    }
}
