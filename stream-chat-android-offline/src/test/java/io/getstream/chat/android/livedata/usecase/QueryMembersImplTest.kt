package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.doReturn
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.BaseConnectedMockedTest
import io.getstream.chat.android.test.TestCall
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.When
import org.amshove.kluent.any
import org.amshove.kluent.calling
import org.amshove.kluent.shouldContainSame
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class QueryMembersImplTest : BaseConnectedMockedTest() {

    /**
     * Tests the integration of domain -> client functionality through queryMembers UseCase.
     * Does not test that the backend respects filters, etc.
     */

    @Test
    fun queryMembers(): Unit = runBlocking {

        val channel = data.channel4 // has > 1 members
        val channelClient = ChannelClient(channel.type, channel.id, client)

        // don't use the default mocked ChannelClient instance - we want a real one to invoke
        // our mocked client queryMembers method.
        When calling client.channel(any(), any()) doReturn channelClient

        // mock query members for the client
        val queryMembersFunction = client.queryMembers(any(), any(), any(), any(), any(), any(), any())
        When calling queryMembersFunction doReturn TestCall(Result(channel.members))

        val result = chatDomain
            .useCases
            .queryMembers(channel.type, channel.id)
            .execute()

        assertSuccess(result)

        val actualUserIds = result.data().map { it.getUserId() }
        val expectedUserIds = channel.members.map { it.getUserId() }

        actualUserIds shouldContainSame expectedUserIds
    }
}
