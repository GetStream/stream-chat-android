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

package io.getstream.realm.filter

import com.squareup.moshi.Moshi
import com.squareup.moshi.addAdapter
import io.getstream.chat.android.models.Filters
import io.getstream.realm.entity.FilterNode
import io.getstream.realm.moshi.FilterNodeAdapter
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

@OptIn(ExperimentalStdlibApi::class)
internal class RealmFilterSerializationTest {

    @Test
    fun `it should be possible to convert filter node to string`() {
        val adapter = Moshi.Builder()
            .addAdapter(FilterNodeAdapter())
            .build()
            .adapter(FilterNode::class.java)

        val filterAsString = Filters.`in`("members", listOf("user_id"))
            .toFilterNode()
            .let(adapter::toJson)

        filterAsString `should be equal to` "{\"filter_type\":\"in\",\"field\":\"members\",\"value\":[\"user_id\"]}"
    }

    @Test
    fun `it should be able to convert string to filter node`() {
        val filterString = "{\"filter_type\":\"in\",\"field\":\"members\",\"value\":[\"user_id\"]}"

        val adapter = Moshi.Builder()
            .addAdapter(FilterNodeAdapter())
            .build()
            .adapter(FilterNode::class.java)

        val filterAsNode = Filters.`in`("members", listOf("user_id")).toFilterNode()
        val expectedFilter = adapter.fromJson(filterString)

        expectedFilter `should be equal to` filterAsNode
    }

    @Test
    fun `it should be possible to convert filter node to string in and out`() {
        val adapter = Moshi.Builder()
            .addAdapter(FilterNodeAdapter())
            .build()
            .adapter(FilterNode::class.java)

        val filter = Filters.`in`("members", listOf("user_id"))
        val filterAsString = filter.toFilterNode().let(adapter::toJson)

        adapter.fromJson(filterAsString)?.toFilterObject() `should be equal to` filter
    }

    @Test
    fun `it should be possible to convert filter node to string in and out - complex case1`() {
        val adapter = Moshi.Builder()
            .addAdapter(FilterNodeAdapter())
            .build()
            .adapter(FilterNode::class.java)

        val filter = Filters.and(
            Filters.eq("type", "messaging"),
            Filters.eq("type", "something"),
        )
        val filterAsString = filter.toFilterNode().let(adapter::toJson)
        val filterNode = adapter.fromJson(filterAsString)
        filterNode?.toFilterObject() `should be equal to` filter
    }

    @Test
    fun `it should be possible to convert filter node to string in and out - complex case12`() {
        val adapter = Moshi.Builder()
            .addAdapter(FilterNodeAdapter())
            .build()
            .adapter(FilterNode::class.java)

        val filter = Filters.and(
            Filters.eq("type", "messaging"),
            Filters.greaterThan("age", 18.0),
        )
        val filterAsString = filter.toFilterNode().let(adapter::toJson)
        val filterNode = adapter.fromJson(filterAsString)
        filterNode?.toFilterObject() `should be equal to` filter
    }

    @Test
    fun `it should be possible to convert filter node to string in and out - complex case2`() {
        val adapter = Moshi.Builder()
            .addAdapter(FilterNodeAdapter())
            .build()
            .adapter(FilterNode::class.java)

        val filter = Filters.and(
            Filters.eq("type", "messaging"),
            Filters.`in`("members", listOf("user_id")),
        )
        val filterAsString = filter.toFilterNode().let(adapter::toJson)
        val filterNode = adapter.fromJson(filterAsString)
        filterNode?.toFilterObject() `should be equal to` filter
    }

    @Test
    fun `it should be possible to convert filter node to string in and out - complex case3`() {
        val adapter = Moshi.Builder()
            .addAdapter(FilterNodeAdapter())
            .build()
            .adapter(FilterNode::class.java)

        val filter = Filters.and(
            Filters.eq("type", "messaging"),
            Filters.`in`("members", listOf("user_id")),
            Filters.or(Filters.notExists("draft"), Filters.eq("draft", false)),
        )

        val filterAsString = filter.toFilterNode().let(adapter::toJson)
        val filterNode = adapter.fromJson(filterAsString)
        filterNode?.toFilterObject() `should be equal to` filter
    }
}
