package io.getstream.chat.android.offline

import android.content.Context
import android.os.Handler
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class IdGenerationDomainTest {

    private lateinit var chatDomainImpl: ChatDomainImpl
    private lateinit var currentUserFake: User

    @BeforeEach
    fun init() {
        val contextMock = mock<Context>()
        val clientMock = mock<ChatClient>()
        val handlerFake = Handler()
        chatDomainImpl = ChatDomainImpl(
            appContext = contextMock,
            client = clientMock,
            handler = handlerFake,
            backgroundSyncEnabled = true,
            offlineEnabled = true,
            recoveryEnabled = false,
            userPresence = true,
        )
        currentUserFake = randomUser()
    }

    @Test
    fun `Given ChatDomainImpl with no currentUser When generateMessageId() called Should throw`() {
        assertThrows<NullPointerException> { chatDomainImpl.user.value!! }
        assertThrows<NullPointerException> { chatDomainImpl.generateMessageId() }
    }

    @Test
    fun `Given ChatDomainImpl with valid currentUser When generateMessageId() called Should return not-empty messageId`() {
        setCurrentUser()

        val messageId = chatDomainImpl.generateMessageId()

        Truth.assertThat(messageId).isNotEmpty()
    }

    @Test
    fun `Given ChatDomainImpl with valid currentUser When generateMessageId() called Should return unique messageId`() {
        setCurrentUser()

        val idMap = sortedSetOf<String>()
        (0..1000000).forEach {
            val messageId = chatDomainImpl.generateMessageId()
            Truth.assertThat(idMap).doesNotContain(messageId)
            idMap.add(messageId)
        }
    }

    private fun setCurrentUser() {
        chatDomainImpl.currentUser = currentUserFake
        Truth.assertThat(chatDomainImpl.currentUser).isNotNull()
    }
}
