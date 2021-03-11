package io.getstream.chat.android.livedata.utils

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.NeutralFilterObject
import io.getstream.chat.android.client.models.CustomObject
import io.getstream.chat.android.livedata.randomChannel
import org.amshove.kluent.`should contain same`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class CustomObjectFilteringTest {

    /** [filterArguments] */
    @ParameterizedTest
    @MethodSource("filterArguments")
    fun `CustomObject should be filtered by the given FilterObject`(
        customObjectList: List<CustomObject>,
        filterObject: FilterObject,
        expectedListOfObject: List<CustomObject>,
    ) {
        val result = customObjectList.filter(filterObject)

        result `should contain same` expectedListOfObject
    }

    companion object {

        @JvmStatic
        fun filterArguments() = neutralFilterArguments()

        @JvmStatic
        fun neutralFilterArguments() = listOf(
            List(10) { randomChannel() }.let { expectedList ->
                Arguments.of(expectedList, NeutralFilterObject, expectedList)
            }
        )
    }
}
