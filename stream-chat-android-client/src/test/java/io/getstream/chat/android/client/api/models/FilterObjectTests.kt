package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.models.Filters
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should not be equal to`
import org.junit.jupiter.api.Test

internal class FilterObjectTests {

    @Test
    fun `Two filters with different arguments Should not be equal`() {
        val filterObject1 = Filters.`in`("members", listOf("userId1", "userId2"))
        val filterObject2 = Filters.`in`("members", listOf("userId1", "userId3"))

        filterObject1 `should not be equal to` filterObject2
    }

    @Test
    fun `Two filters with different types Should not be equal`() {
        val filterObject1 = Filters.`in`("members", listOf("userId1"))
        val filterObject2 = Filters.nin("members", listOf("userId1"))

        filterObject1 `should not be equal to` filterObject2
    }

    @Test
    fun `Two different filter objects Should be equal`() {
        val filterObject1 = Filters.`in`("members", listOf("userId1", "userId2"))
        val filterObject2 = Filters.`in`("members", listOf("userId1", "userId2"))

        filterObject1 `should be equal to` filterObject2
    }
}
