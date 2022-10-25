package io.getstream.realm.filter

import com.squareup.moshi.Moshi
import com.squareup.moshi.addAdapter
import io.getstream.chat.android.client.models.Filters
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
}
