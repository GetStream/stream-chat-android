package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.persistance.repository.ReactionRepository
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.test.randomUser
import io.getstream.chat.android.test.randomCID
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
internal class DeleteReactionListenerDatabaseTest {

    private val clientState = mock<ClientState>()
    private val reactionRepository = mock<ReactionRepository>()
    private val messageRepository = mock<MessageRepository>()

    private val deleteReactionListenerDatabase = DeleteReactionListenerDatabase(
        clientState, reactionRepository, messageRepository
    )

    @Test
    fun `when deleting reactions, the reactions repository should be updated`() = runTest {

        deleteReactionListenerDatabase.onDeleteReactionRequest(
            randomCID(),
            randomString(),
            randomString(),
            randomUser()
        )
    }
}
