package io.getstream.chat.android.client.api.models

import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import java.util.UUID

internal class QueryChannelRequestTests {

    @Test
    fun `ensure 'withMembers' sets 'state' to True`() {
        QueryChannelRequest().withMembers(any(), any()).apply {
            state `should be equal to` true
        }
    }

    @Test
    fun `ensure 'withMessages' sets 'state' to True`() {
        QueryChannelRequest().withMessages(any()).apply {
            state `should be equal to` true
        }
    }

    @ParameterizedTest
    @MethodSource("generatePaginationList")
    fun `ensure paginated 'withMessages' sets 'state' to True`(pagination: Pagination) {
        val messageId = UUID.randomUUID().toString()
        QueryChannelRequest().withMessages(pagination, messageId, any()).apply {
            state `should be equal to` true
        }
    }

    @Test
    fun `ensure 'withWatchers' sets 'state' to True`() {
        QueryChannelRequest().withWatchers(any(), any()).apply {
            state `should be equal to` true
        }
    }

    internal companion object {
        @JvmStatic
        fun generatePaginationList() = Pagination.values().toList()
    }

}