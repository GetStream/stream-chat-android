package io.getstream.chat.android.offline.repository.facade

import androidx.annotation.CallSuper
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.offline.repository.RepositoryFacade
import io.getstream.chat.android.offline.repository.domain.channel.ChannelRepository
import io.getstream.chat.android.offline.repository.domain.channelconfig.ChannelConfigRepository
import io.getstream.chat.android.offline.repository.domain.message.MessageRepository
import io.getstream.chat.android.offline.repository.domain.queryChannels.QueryChannelsRepository
import io.getstream.chat.android.offline.repository.domain.reaction.ReactionRepository
import io.getstream.chat.android.offline.repository.domain.syncState.SyncStateRepository
import io.getstream.chat.android.offline.repository.domain.user.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.BeforeEach

@ExperimentalCoroutinesApi
internal open class BaseRepositoryFacadeTest {

    protected lateinit var users: UserRepository
    protected lateinit var configs: ChannelConfigRepository
    protected lateinit var channels: ChannelRepository
    protected lateinit var queryChannels: QueryChannelsRepository
    protected lateinit var messages: MessageRepository
    protected lateinit var reactions: ReactionRepository
    protected lateinit var syncState: SyncStateRepository

    protected val scope = TestCoroutineScope()

    protected lateinit var sut: RepositoryFacade

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

        sut = RepositoryFacade(
            userRepository = users,
            configsRepository = configs,
            channelsRepository = channels,
            queryChannelsRepository = queryChannels,
            messageRepository = messages,
            reactionsRepository = reactions,
            syncStateRepository = syncState,
            scope = scope,
            defaultConfig = mock(),
        )
    }
}
