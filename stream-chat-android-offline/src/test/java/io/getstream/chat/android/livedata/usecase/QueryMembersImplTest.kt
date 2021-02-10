package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.BaseConnectedMockedTest
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.getOrAwaitValue
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.Verify
import org.amshove.kluent.When
import org.amshove.kluent.any
import org.amshove.kluent.called
import org.amshove.kluent.calling
import org.amshove.kluent.on
import org.amshove.kluent.shouldContainSame
import org.amshove.kluent.shouldNotBeEmpty
import org.amshove.kluent.that
import org.amshove.kluent.was
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class QueryMembersImplTest : BaseConnectedMockedTest() {

    /**
     * Tests the integration of domain -> client functionality through queryMembers UseCase.
     * Does not test that the backend respects filters, etc.
     */

    @Test
    fun `query members should make a client api call when online`(): Unit = runBlocking {

        val channel = data.channel4 // has > 1 members
        val channelClientSpy = spy(ChannelClient(channel.type, channel.id, client))

        // don't use the default mocked ChannelClient instance - we want a real one to call through
        // to our mocked client
        When calling client.channel(any(), any()) doReturn channelClientSpy

        // mock query members result for the client method
        When calling client.queryMembers(any(), any(), any(), any(), any(), any(), any()) doReturn TestCall(
            Result(
                channel.members
            )
        )

        val result = chatDomain
            .useCases
            .queryMembers(channel.type, channel.id)
            .execute()

        assertSuccess(result)

        Verify on client that client.queryMembers(any(), any(), any(), any(), any(), any(), any()) was called
        Verify on channelClientSpy that channelClientSpy.queryMembers(any(), any(), any(), any(), any()) was called

        val actualUserIds = result.data().map { it.getUserId() }
        val expectedUserIds = channel.members.map { it.getUserId() }

        actualUserIds shouldContainSame expectedUserIds
    }

    @Test
    fun `query members should return current member list when offline`(): Unit = runBlockingTest {

        chatDomainImpl.setOffline()
        val channel = data.channel4 // has > 1 members
        val channelController = chatDomainImpl.channel(channel.cid)
        channelController.updateLiveDataFromChannel(channel) // make sure controller has data
        val expectedMembers = channelController.members.getOrAwaitValue()

        val result = chatDomain
            .useCases
            .queryMembers(channel.type, channel.id)
            .execute()

        advanceUntilIdle()

        assertSuccess(result)

        // verify we never call through to client when offline
        verify(client, never()).queryMembers(any(), any(), any(), any(), any(), any(), any())

        val actualUserIds = result.data().map { it.getUserId() }
        val expectedUserIds = expectedMembers.map { it.getUserId() }

        actualUserIds.shouldNotBeEmpty()
        actualUserIds shouldContainSame expectedUserIds
    }
}
