package io.getstream.realm.filter

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import io.getstream.chat.android.client.models.Filters
import io.getstream.realm.entity.FilterNode
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class RealmFilterSerializationTest {

    @Test
    fun `it should be possible to convert filter node to string`() {
        val adapter = Moshi.Builder().build().adapter(FilterNode::class.java)

        val filterAsString = Filters.`in`("members", listOf("user_id"))
            .toFilterNode()
            .let(adapter::toJson)

        filterAsString `should be equal to` "{\"filter_type\":\"in\",\"field\":\"members\",\"value\":[\"user_id\"]}"
    }

    @Test
    fun `it should be possible to convert filter node to string in and out`() {
        val adapter = Moshi.Builder().build().adapter(FilterNode::class.java)

        val filter = Filters.`in`("members", listOf("user_id"))
        val filterAsString = filter.toFilterNode().let(adapter::toJson)

        adapter.fromJson(filterAsString)?.toFilterObject() `should be equal to` filter
    }

    @Test
    fun `it should be possible to convert filter node to string in and out - complex case`() {
        val adapter = Moshi.Builder().build().adapter(FilterNode::class.java)

        val filter = Filters.and(
            Filters.eq("type", "messaging"),
            Filters.`in`("members", listOf("user_id")),
        )
        val filterAsString = filter.toFilterNode().let(adapter::toJson)
        val filterNode = adapter.fromJson(filterAsString)
        filterNode?.toFilterObject() `should be equal to` filter
    }
}
