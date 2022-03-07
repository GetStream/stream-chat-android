package io.getstream.chat.android.offline.usecase

import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.querychannels.QueryChannelsController
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldBeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
internal class QueryChannelsTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private lateinit var domain: ChatDomainImpl
    private lateinit var queryChannelsController: QueryChannelsController

    private lateinit var queryChannels: QueryChannels

    @BeforeEach
    fun init() {
        queryChannelsController = mock()
        domain = mock() { on { scope } doReturn testCoroutines.scope }
        whenever(domain.queryChannels(any(), any())) doReturn queryChannelsController

        queryChannels = QueryChannels(domain)
    }

    @Test
    fun `When invoked Should return result containing QueryChannelsController instance`() {
        val anyFilter = Filters.neutral()

        val result = queryChannels.invoke(anyFilter, QuerySort()).execute()

        result.data().shouldBeInstanceOf<QueryChannelsController>()
        result.isSuccess.shouldBeTrue()
    }

    @Test
    fun `Given channelsLimit = 0 When invoked Should not query controller`() {
        val anyFilter = Filters.neutral()

        queryChannels.invoke(anyFilter, QuerySort(), limit = 0).execute()

        verifyNoInteractions(queryChannelsController)
    }

    @Test
    fun `Given channelsLimit greater than 0 When invoked Should query controller`() = runBlockingTest {
        val anyFilter = Filters.neutral()
        val channelsLimit = 10
        val messagesLimit = 10

        queryChannels.invoke(anyFilter, QuerySort(), limit = channelsLimit, messageLimit = messagesLimit).execute()

        verify(queryChannelsController).query(channelsLimit, messagesLimit)
    }
}
