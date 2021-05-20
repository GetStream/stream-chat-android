package io.getstream.chat.android.client.helpers

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.Mother
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.clientstate.SocketState
import io.getstream.chat.android.client.clientstate.SocketStateService
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import org.amshove.kluent.`should be`
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

internal class QueryChannelsPostponeHelperTests {

    private lateinit var api: ChatApi
    private lateinit var socketStateService: SocketStateService

    private lateinit var sut: QueryChannelsPostponeHelper

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()

        private const val ATTEMPTS_COUNT = 2
        private const val DELAY_DURATION = 30L
    }

    @BeforeEach
    fun setUp() {
        api = mock()
        socketStateService = mock()
        sut = QueryChannelsPostponeHelper(api, socketStateService, testCoroutines.scope, DELAY_DURATION, ATTEMPTS_COUNT)
    }

    @Test
    fun `Given connected state When query channel Should return channel from api`() {
        val expectedResult = Mother.randomChannel()
        whenever(api.queryChannel(any(), any(), any())) doReturn expectedResult.asCall()
        whenever(socketStateService.state) doReturn SocketState.Connected(Mother.randomString())

        val result = sut.queryChannel("channelType", "channelId", mock()).execute().data()

        verify(api).queryChannel(any(), any(), any())
        result shouldBeEqualTo expectedResult
    }

    @Test
    fun `Given idle connection state When query channel Should return a Error Call`() {
        val expectedErrorResult =
            "Failed to perform job. Waiting for set user completion was too long. Limit of attempts was reached."
        whenever(socketStateService.state) doReturn SocketState.Idle

        val result = sut.queryChannel("channelType", "channelId", mock()).execute().error()
        result.message `should be` expectedErrorResult
    }

    @Test
    fun `Given long pending socket state When query channel Should return a Error Call`() {
        val expectedErrorResult =
            "Failed to perform job. Waiting for set user completion was too long. Limit of attempts was reached."
        whenever(socketStateService.state) doReturn SocketState.Pending

        val result = sut.queryChannel("channelType", "channelId", mock()).execute().error()
        result.message `should be` expectedErrorResult
    }

    @Test
    fun `Given pending state and connected then When query channel Should query to api and return result`() {
        val expectedResult = Mother.randomChannel()
        whenever(api.queryChannel(any(), any(), any())).thenReturn(expectedResult.asCall())
        whenever(socketStateService.state)
            .thenReturn(SocketState.Pending)
            .thenReturn(SocketState.Connected(Mother.randomString()))

        val result = sut.queryChannel("channelType", "channelId", mock()).execute().data()

        verify(api).queryChannel(any(), any(), any())
        result shouldBeEqualTo expectedResult
    }
}
