/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.state.internal.extensions

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.getstream.chat.android.client.extensions.internal.applyPagination
import io.getstream.chat.android.client.parser2.adapters.DateAdapter
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.state.model.querychannels.pagination.internal.QueryChannelsPaginationRequest
import io.getstream.chat.android.state.model.querychannels.pagination.internal.toAnyChannelPaginationRequest
import okio.buffer
import okio.source
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.io.File
import java.util.Date

internal class ChannelExtensionsTest {

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun `When apply pagination Should not throw any exception`() {
        val channelsFile = File(this.javaClass.classLoader!!.getResource("channels.json").toURI())
        val moshi = Moshi.Builder()
            .add(DateAdapter())
            .add(
                File::class.java,
                object : JsonAdapter<File>() {
                    // Dummy adapter because reflective serialization can't deal with platform types
                    override fun fromJson(reader: JsonReader): File? = null
                    override fun toJson(writer: JsonWriter, value: File?) = TODO("Not implemented")
                },
            )
            .add(
                Attachment.UploadState::class.java,
                object : JsonAdapter<Attachment.UploadState>() {
                    // Dummy adapter because reflective serialization can't deal with sealed classes
                    override fun fromJson(reader: JsonReader): Attachment.UploadState? = null
                    override fun toJson(writer: JsonWriter, value: Attachment.UploadState?) = TODO("Not implemented")
                },
            )
            .add(KotlinJsonAdapterFactory())
            .build()
        val adapter = moshi.adapter<List<Channel>>()
        val channels = requireNotNull(adapter.fromJson(JsonReader.of(channelsFile.source().buffer())))
        val sort = QuerySortByField<Channel>().desc(fieldName = "lastMessageAt")
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
        val firstChannel = randomChannel(
            messages = listOf(
                randomMessage(
                    createdAt = Date(1000),
                    parentId = null,
                ),
            ),
        )
        val secondChannel = randomChannel(
            messages = listOf(
                randomMessage(
                    createdAt = Date(3000),
                    parentId = null,
                ),
            ),
        )
        val thirdChannel = randomChannel(
            messages = listOf(
                randomMessage(
                    createdAt = Date(2000),
                    parentId = null,
                ),
            ),
        )
        val sort = QuerySortByField.descByName<Channel>("lastMessageAt")
        val queryPaginationRequest = QueryChannelsPaginationRequest(
            sort = sort,
            channelOffset = 0,
            channelLimit = 30,
            messageLimit = 10,
            memberLimit = 30,
        )

        val result1 = listOf(
            firstChannel,
            secondChannel,
            thirdChannel,
        ).applyPagination(queryPaginationRequest.toAnyChannelPaginationRequest())

        result1.first() shouldBeEqualTo secondChannel
        result1.last() shouldBeEqualTo firstChannel
    }

    @Test
    fun `When apply pagination Should sort correctly ascending`() {
        val firstChannel = randomChannel(
            messages = listOf(
                randomMessage(
                    createdAt = Date(1000),
                    parentId = null,
                ),
            ),
        )
        val secondChannel = randomChannel(
            messages = listOf(
                randomMessage(
                    createdAt = Date(3000),
                    parentId = null,
                ),
            ),
        )
        val thirdChannel = randomChannel(
            messages = listOf(
                randomMessage(
                    createdAt = Date(2000),
                    parentId = null,
                ),
            ),
        )
        val sort = QuerySortByField.ascByName<Channel>("lastMessageAt")
        val queryPaginationRequest = QueryChannelsPaginationRequest(
            sort = sort,
            channelOffset = 0,
            channelLimit = 30,
            messageLimit = 10,
            memberLimit = 30,
        )

        val result1 = listOf(
            firstChannel,
            secondChannel,
            thirdChannel,
        ).applyPagination(queryPaginationRequest.toAnyChannelPaginationRequest())

        result1.first() shouldBeEqualTo firstChannel
        result1.last() shouldBeEqualTo secondChannel
    }
}
