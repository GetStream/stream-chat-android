package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.test.randomMessage
import io.getstream.chat.android.client.test.randomReaction
import io.getstream.chat.android.client.test.randomUser
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.plugin.logic.channel.internal.ChannelLogic
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.test.randomCID
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
internal class DeleteReactionListenerStateTest {
    private val user = randomUser()
    private val defaultReaction = randomReaction(
        userId = user.id,
        user = user
    )
    private val defaultMessage = randomMessage(
        ownReactions = mutableListOf(defaultReaction),
        latestReactions = mutableListOf(defaultReaction),
        user = user
    )

    private val clientState = mock<ClientState>()
    private val channelLogic = mock<ChannelLogic> {
        on(it.getMessage(any())) doReturn defaultMessage
    }
    private val logicRegistry = mock<LogicRegistry> {
        on(it.channelFromMessageId(any())) doReturn channelLogic
        on(it.channel(any(), any())) doReturn channelLogic
    }

    private val deleteReactionListenerDatabase = DeleteReactionListenerState(logicRegistry, clientState)

    @Test
    fun `when deleting reactions, reactions should be optimistically be deleted`() = runTest {
        whenever(clientState.isNetworkAvailable) doReturn true

        deleteReactionListenerDatabase.onDeleteReactionRequest(
            cid = randomCID(),
            messageId = defaultMessage.id,
            reactionType = defaultReaction.type,
            currentUser = user
        )

        verify(channelLogic).upsertMessage(argThat { message ->
            message.ownReactions.isEmpty() && message.latestReactions.isEmpty()
        })
    }

}
