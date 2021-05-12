package io.getstream.chat.android.offline.usecase

import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.querychannels.QueryChannelsController
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

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

        Truth.assertThat(result.data()).isInstanceOf(QueryChannelsController::class.java)
        Truth.assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `Given channelsLimit = 0 When invoked Should not query controller`() {
        val anyFilter = Filters.neutral()

        queryChannels.invoke(anyFilter, QuerySort(), limit = 0).execute()

        verifyZeroInteractions(queryChannelsController)
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
