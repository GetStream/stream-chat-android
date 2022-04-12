package io.getstream.chat.android.offline.repository.facade

import androidx.annotation.CallSuper
import io.getstream.chat.android.offline.repository.builder.internal.RepositoryFacade
import io.getstream.chat.android.offline.repository.domain.channel.internal.ChannelRepository
import io.getstream.chat.android.offline.repository.domain.channelconfig.internal.ChannelConfigRepository
import io.getstream.chat.android.offline.repository.domain.message.attachment.internal.AttachmentRepository
import io.getstream.chat.android.offline.repository.domain.message.internal.MessageRepository
import io.getstream.chat.android.offline.repository.domain.queryChannels.internal.QueryChannelsRepository
import io.getstream.chat.android.offline.repository.domain.reaction.internal.ReactionRepository
import io.getstream.chat.android.offline.repository.domain.syncState.internal.SyncStateRepository
import io.getstream.chat.android.offline.repository.domain.user.internal.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.mock

@ExperimentalCoroutinesApi
internal open class BaseRepositoryFacadeTest {

    protected lateinit var users: UserRepository
    protected lateinit var configs: ChannelConfigRepository
    protected lateinit var channels: ChannelRepository
    protected lateinit var queryChannels: QueryChannelsRepository
    protected lateinit var messages: MessageRepository
    protected lateinit var reactions: ReactionRepository
    protected lateinit var syncState: SyncStateRepository
    protected lateinit var attachmentRepository: AttachmentRepository

    protected val scope = TestScope()

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
        attachmentRepository = mock()

        sut = RepositoryFacade(
            userRepository = users,
            configsRepository = configs,
            channelsRepository = channels,
            queryChannelsRepository = queryChannels,
            messageRepository = messages,
            reactionsRepository = reactions,
            syncStateRepository = syncState,
            attachmentRepository = attachmentRepository,
            scope = scope,
            defaultConfig = mock(),
        )
    }
}
