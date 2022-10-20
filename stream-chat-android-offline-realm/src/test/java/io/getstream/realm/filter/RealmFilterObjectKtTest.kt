package io.getstream.realm.filter

import io.getstream.chat.android.client.models.Filters
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class RealmFilterObjectKtTest {

    @Test
    fun `it should be possible to convert a filter object to realm`() {
        val filterObject = Filters.and(
            Filters.eq("name", "leandro"),
            Filters.exists("age")
        ).toFilterNodeEntity()

        filterObject.filter_type `should be equal to` KEY_AND
    }

    @Test
    fun `it should be possible to convert and revert a filter object to realm`() {
        val filterObject = Filters.and(
            Filters.eq("name", "leandro"),
            Filters.exists("age")
        )

        val newFilter = filterObject.toFilterNodeEntity().toFilterObject()

        newFilter `should be equal to` filterObject
    }

    @Test
    fun `it should be possible to convert and revert a filter object to realm - complex scenario`() {
        val filterObject = Filters.and(
            Filters.eq("name", "leandro"),
            Filters.or(
                Filters.greaterThan("age", "18"),
                Filters.contains("profession", "programmer")
            ),
            Filters.ne("something", "something")
        )

        val newFilter = filterObject.toFilterNodeEntity().toFilterObject()

        newFilter `should be equal to` filterObject
    }

}
