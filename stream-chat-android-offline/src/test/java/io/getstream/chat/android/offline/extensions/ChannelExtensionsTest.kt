package io.getstream.chat.android.offline.extensions

import com.google.gson.reflect.TypeToken
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.parser.StreamGson
import io.getstream.chat.android.offline.randomChannel
import io.getstream.chat.android.offline.request.QueryChannelsPaginationRequest
import io.getstream.chat.android.offline.request.toAnyChannelPaginationRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File
import java.util.Date

internal class ChannelExtensionsTest {

    @Test
    fun `When apply pagination Should not throw any exception`() {
        val channelsFile = File(this.javaClass.classLoader!!.getResource("channels.json").toURI())
        val type = object : TypeToken<List<Channel>>() {}.type
        val channels = StreamGson.gson.fromJson<Collection<Channel>>(channelsFile.reader(), type)
        val sort = QuerySort<Channel>().desc(Channel::lastMessageAt)
        val queryPaginationRequest = QueryChannelsPaginationRequest(
            sort = sort,
            channelOffset = 0,
            channelLimit = 30,
            messageLimit = 10,
            memberLimit = 30,
        )

        channels.applyPagination(queryPaginationRequest.toAnyChannelPaginationRequest())
    }

    @Test
    fun `When apply pagination Should sort correctly descending`() {
        val firstChannel = randomChannel(lastMessageAt = Date(1000))
        val secondChannel = randomChannel(lastMessageAt = Date(3000))
        val thirdChannel = randomChannel(lastMessageAt = Date(2000))
        val sort = QuerySort<Channel>().desc(Channel::lastMessageAt)
        val queryPaginationRequest = QueryChannelsPaginationRequest(
            sort = sort,
            channelOffset = 0,
            channelLimit = 30,
            messageLimit = 10,
            memberLimit = 30,
        )

        val result1 = listOf(firstChannel, secondChannel, thirdChannel).applyPagination(queryPaginationRequest.toAnyChannelPaginationRequest())

        Assertions.assertTrue {
            result1.first() == secondChannel && result1.last() == firstChannel
        }
    }

    @Test
    fun `When apply pagination Should sort correctly ascending`() {
        val firstChannel = randomChannel(lastMessageAt = Date(1000))
        val secondChannel = randomChannel(lastMessageAt = Date(3000))
        val thirdChannel = randomChannel(lastMessageAt = Date(2000))
        val sort = QuerySort<Channel>().asc(Channel::lastMessageAt)
        val queryPaginationRequest = QueryChannelsPaginationRequest(
            sort = sort,
            channelOffset = 0,
            channelLimit = 30,
            messageLimit = 10,
            memberLimit = 30,
        )

        val result1 = listOf(firstChannel, secondChannel, thirdChannel).applyPagination(queryPaginationRequest.toAnyChannelPaginationRequest())

        Assertions.assertTrue {
            result1.first() == firstChannel && result1.last() == secondChannel
        }
    }
}
