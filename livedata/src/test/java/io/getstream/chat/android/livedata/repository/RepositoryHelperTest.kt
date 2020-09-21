package io.getstream.chat.android.livedata.repository

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.livedata.ChatDatabase
import io.getstream.chat.android.livedata.randomUser
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class RepositoryHelperTests {

    private lateinit var chatClient: ChatClient
    private lateinit var chatDatabase: ChatDatabase
    private lateinit var sut: RepositoryHelper

    @BeforeEach
    fun setUp() {
        chatClient = mock()
        chatDatabase = mock {
            on { queryChannelsQDao() } doReturn mock()
            on { userDao() } doReturn mock()
            on { reactionDao() } doReturn mock()
            on { messageDao() } doReturn mock()
            on { channelStateDao() } doReturn mock()
            on { channelConfigDao() } doReturn mock()
            on { syncStateDao() } doReturn mock()
        }
        sut = RepositoryHelper(chatClient, randomUser(), chatDatabase)
    }

    @Test
    fun test1() {
        sut.shouldNotBeNull()
    }
}