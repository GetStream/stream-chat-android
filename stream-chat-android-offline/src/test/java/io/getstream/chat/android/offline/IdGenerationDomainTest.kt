package io.getstream.chat.android.offline

import android.content.Context
import android.os.Handler
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.repository.database.ChatDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [28])
internal class IdGenerationDomainTest {

    private lateinit var chatDomainImpl: ChatDomainImpl
    private lateinit var currentUserFake: User

    private lateinit var chatDatabase: ChatDatabase

    @Before
    fun init() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        currentUserFake = randomUser()
        val clientMock = mock<ChatClient> {
            on(it.getCurrentUser()) doReturn currentUserFake
        }
        val handlerFake = Handler()
        chatDomainImpl = ChatDomainImpl(
            appContext = context,
            client = clientMock,
            handler = handlerFake,
            backgroundSyncEnabled = true,
            offlineEnabled = true,
            recoveryEnabled = false,
            userPresence = true,
        )
    }

    @Test
    fun `Given ChatDomainImpl with valid currentUser When generateMessageId() called Should return not-empty messageId`() {
            val messageId = chatDomainImpl.generateMessageId()

            Truth.assertThat(messageId).isNotEmpty()
        }

    @Test
    fun `Given ChatDomainImpl with valid currentUser When generateMessageId() called Should return unique messageId`() {
            val idMap = sortedSetOf<String>()
            repeat(1000000) {
                val messageId = chatDomainImpl.generateMessageId()
                Truth.assertThat(idMap).doesNotContain(messageId)
                idMap.add(messageId)
            }
        }
}
