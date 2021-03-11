package io.getstream.chat.android.livedata.utils

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.NeutralFilterObject
import io.getstream.chat.android.client.models.CustomObject
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.livedata.randomChannel
import io.getstream.chat.android.livedata.randomMember
import io.getstream.chat.android.test.positiveRandomInt
import io.getstream.chat.android.test.randomInt
import io.getstream.chat.android.test.randomIntBetween
import io.getstream.chat.android.test.randomLong
import io.getstream.chat.android.test.randomLongBetween
import io.getstream.chat.android.test.randomString
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

        val stringQuery = randomString(10)
        val intQuery = randomIntBetween(-100, 300)
        val longQuery = randomLongBetween(-300, 100)

        @JvmStatic
        fun filterArguments() = neutralFilterArguments() +
            distinctFilterArguments() +
            containsFilterArguments()

        @JvmStatic
        fun neutralFilterArguments() = listOf(
            List(10) { randomChannel() }.let { expectedList ->
                Arguments.of(expectedList, NeutralFilterObject, expectedList)
            }
        )
        @JvmStatic
        fun distinctFilterArguments() = List(positiveRandomInt(10)) { randomMember() }.let { memberList ->
            listOf(
                List(positiveRandomInt(10)) {
                    randomChannel(
                        id = "!members${randomString()}",
                        members = memberList
                    )
                }.let { expectedList ->
                    Arguments.of(
                        expectedList + List(10) { randomChannel() },
                        Filters.distinct(memberList.map { it.user.id }),
                        expectedList
                    )
                },
                List(positiveRandomInt(10)) {
                    randomChannel(
                        id = "!members${randomString()}",
                        members = memberList
                    )
                }.let { expectedList ->
                    Arguments.of(
                        expectedList + List(10) {
                            randomChannel(
                                members = memberList
                            )
                        },
                        Filters.distinct(memberList.map { it.user.id }),
                        expectedList
                    )
                },
                List(positiveRandomInt(10)) {
                    randomChannel(
                        id = "!membersaaa",
                        members = memberList
                    )
                }.let { expectedList ->
                    Arguments.of(
                        expectedList + List(10) {
                            randomChannel(
                                id = "!membersaaa",
                            )
                        },
                        Filters.distinct(memberList.map { it.user.id }),
                        expectedList
                    )
                },
                List(positiveRandomInt(10)) {
                    randomChannel(
                        id = "!membersa",
                        members = memberList
                    )
                }.let { expectedList ->
                    Arguments.of(
                        expectedList + List(10) {
                            randomChannel(
                                id = "!membersa",
                                members = memberList.drop(positiveRandomInt(memberList.size))
                            )
                        },
                        Filters.distinct(memberList.map { it.user.id }),
                        expectedList
                    )
                }
            )
        }

        @JvmStatic
        fun containsFilterArguments() = listOf(
            List(positiveRandomInt(10)) {
                randomChannel().apply {
                    extraData["someField"] = (List(positiveRandomInt(10)) { randomLong() } + longQuery).shuffled()
                }
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(10) { randomChannel() }).shuffled(),
                    Filters.contains("someField", longQuery),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel().apply {
                    extraData["someField"] = (List(positiveRandomInt(10)) { randomString() } + stringQuery).shuffled()
                }
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(10) { randomChannel() }).shuffled(),
                    Filters.contains("someField", stringQuery),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel().apply {
                    extraData["someField"] = (List(positiveRandomInt(10)) { randomInt() } + intQuery).shuffled()
                }
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(10) { randomChannel() }).shuffled(),
                    Filters.contains("someField", intQuery),
                    expectedList,
                )
            },
        )
    }
}
