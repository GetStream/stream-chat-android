package io.getstream.chat.android.offline

import android.content.Context
import android.os.Handler
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.experimental.global.GlobalMutableState
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldNotBeEmpty
import org.amshove.kluent.shouldNotBeNull
import org.amshove.kluent.shouldNotContain
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

internal class IdGenerationDomainTest {

    private lateinit var chatDomainImpl: ChatDomainImpl
    private lateinit var currentUserFake: User
    private lateinit var globalMutableState: GlobalMutableState

    @BeforeEach
    fun init() {
        val contextMock = mock<Context>()
        val clientMock = mock<ChatClient>()
        val handlerFake = Handler()
        globalMutableState = GlobalMutableState.create()

        chatDomainImpl = ChatDomainImpl(
            appContext = contextMock,
            client = clientMock,
            handler = handlerFake,
            backgroundSyncEnabled = true,
            recoveryEnabled = false,
            userPresence = true,
            globalState = globalMutableState
        )
        currentUserFake = randomUser()
    }

    @Test
    fun `Given ChatDomainImpl with no currentUser When generateMessageId() called Should throw`() {
        invoking { chatDomainImpl.user.value!! }.shouldThrow(NullPointerException::class)
        invoking { chatDomainImpl.generateMessageId() }.shouldThrow(NullPointerException::class)
    }

    @Test
    fun `Given ChatDomainImpl with valid currentUser When generateMessageId() called Should return not-empty messageId`() {
        setCurrentUser()

        val messageId = chatDomainImpl.generateMessageId()

        messageId.shouldNotBeEmpty()
    }

    @Test
    fun `Given ChatDomainImpl with valid currentUser When generateMessageId() called Should return unique messageId`() {
        setCurrentUser()

        val idMap = sortedSetOf<String>()
        repeat(1000000) {
            val messageId = chatDomainImpl.generateMessageId()
            idMap shouldNotContain messageId
            idMap.add(messageId)
        }
    }

    private fun setCurrentUser() {
        globalMutableState._user.value = currentUserFake
        chatDomainImpl.userConnected(currentUserFake)
        chatDomainImpl.user.value.shouldNotBeNull()
    }
}
