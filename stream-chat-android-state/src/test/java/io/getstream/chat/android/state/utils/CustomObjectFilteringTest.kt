/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.state.utils

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.CustomObject
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.NeutralFilterObject
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomIntBetween
import io.getstream.chat.android.randomLong
import io.getstream.chat.android.randomLongBetween
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomSyncStatus
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.state.utils.internal.filter
import org.amshove.kluent.shouldContainSame
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

        result shouldContainSame expectedListOfObject
    }

    companion object {

        val stringQuery = randomString(10)
        val intQuery = randomIntBetween(-100, 300)
        val longQuery = randomLongBetween(-300, 100)
        val memberIds = List(positiveRandomInt(10)) { randomString(10) }

        @JvmStatic
        fun filterArguments() = neutralFilterArguments() +
            distinctFilterArguments() +
            containsFilterArguments() +
            autocompleteFilterArguments() +
            existsFilterArguments() +
            notExistsFilterArguments() +
            equalsFilterArguments() +
            notEqualsFilterArguments() +
            greaterThanFilterArguments() +
            greaterThanOrEqualsFilterArguments() +
            lessThanFilterArguments() +
            lessThanOrEqualsFilterArguments() +
            inFilterArguments() +
            notInFilterArguments() +
            andFilterArguments() +
            norFilterArguments() +
            orFilterArguments()

        @JvmStatic
        fun neutralFilterArguments() = listOf(
            List(10) { randomChannel() }.let { expectedList ->
                Arguments.of(expectedList, NeutralFilterObject, expectedList)
            },
        )

        @JvmStatic
        fun distinctFilterArguments() = List(positiveRandomInt(10)) { randomMember() }.let { memberList ->
            listOf(
                List(positiveRandomInt(10)) {
                    randomChannel(
                        id = "!members${randomString()}",
                        members = memberList,
                    )
                }.let { expectedList ->
                    Arguments.of(
                        expectedList + List(10) { randomChannel() },
                        Filters.distinct(memberList.map { it.user.id }),
                        expectedList,
                    )
                },
                List(positiveRandomInt(10)) {
                    randomChannel(
                        id = "!members${randomString()}",
                        members = memberList,
                    )
                }.let { expectedList ->
                    Arguments.of(
                        expectedList + List(10) {
                            randomChannel(
                                members = memberList,
                            )
                        },
                        Filters.distinct(memberList.map { it.user.id }),
                        expectedList,
                    )
                },
                List(positiveRandomInt(10)) {
                    randomChannel(
                        id = "!membersaaa",
                        members = memberList,
                    )
                }.let { expectedList ->
                    Arguments.of(
                        expectedList + List(10) {
                            randomChannel(
                                id = "!membersaaa",
                            )
                        },
                        Filters.distinct(memberList.map { it.user.id }),
                        expectedList,
                    )
                },
                List(positiveRandomInt(10)) {
                    randomChannel(
                        id = "!membersa",
                        members = memberList,
                    )
                }.let { expectedList ->
                    Arguments.of(
                        expectedList + List(10) {
                            randomChannel(
                                id = "!membersa",
                                members = memberList.drop(positiveRandomInt(memberList.size)),
                            )
                        },
                        Filters.distinct(memberList.map { it.user.id }),
                        expectedList,
                    )
                },
            )
        }

        @JvmStatic
        fun containsFilterArguments() = listOf(
            memberIds.random().let { memberId ->
                List(positiveRandomInt(10)) {
                    randomChannel(members = (List(positiveRandomInt(10)) { randomMember() } + randomMember(randomUser(id = memberId))).shuffled())
                }.let { expectedList ->
                    Arguments.of(
                        (
                            expectedList + List(10) {
                                randomChannel(members = List(positiveRandomInt(10)) { randomMember() })
                            }
                            ).shuffled(),
                        Filters.contains("members", memberId),
                        expectedList,
                    )
                }
            },
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf(
                        "someField" to (List(positiveRandomInt(10)) { randomLong() } + longQuery).shuffled(),
                    ),
                )
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(10) { randomChannel() }).shuffled(),
                    Filters.contains("someField", longQuery),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf(
                        "someField" to (List(positiveRandomInt(10)) { randomString() } + stringQuery).shuffled(),
                    ),
                )
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(10) { randomChannel() }).shuffled(),
                    Filters.contains("someField", stringQuery),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf(
                        "someField" to (List(positiveRandomInt(10)) { randomInt() } + intQuery).shuffled(),
                    ),
                )
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(10) { randomChannel() }).shuffled(),
                    Filters.contains("someField", intQuery),
                    expectedList,
                )
            },
        )

        @JvmStatic
        fun autocompleteFilterArguments() = listOf(
            List(positiveRandomInt(10)) {
                randomChannel(type = randomString(20) + stringQuery + randomString(20))
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(10) { randomChannel(type = randomString(8)) }).shuffled(),
                    Filters.autocomplete("type", stringQuery),
                    expectedList,
                )
            },
        )

        @JvmStatic
        fun existsFilterArguments() = listOf(
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf("someField" to longQuery),
                )
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(10) { randomChannel() }).shuffled(),
                    Filters.exists("someField"),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf("someField" to stringQuery),
                )
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(10) { randomChannel() }).shuffled(),
                    Filters.exists("someField"),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf("someField" to intQuery),
                )
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(10) { randomChannel() }).shuffled(),
                    Filters.exists("someField"),
                    expectedList,
                )
            },
        )

        @JvmStatic
        fun notExistsFilterArguments() = listOf(
            List(10) { randomChannel() }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel(
                                extraData = mapOf("someField" to longQuery),
                            )
                        }
                        ).shuffled(),
                    Filters.notExists("someField"),
                    expectedList,
                )
            },
            List(10) { randomChannel() }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel(
                                extraData = mapOf("someField" to stringQuery),
                            )
                        }
                        ).shuffled(),
                    Filters.notExists("someField"),
                    expectedList,
                )
            },
            List(10) { randomChannel() }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel(
                                extraData = mapOf("someField" to intQuery),
                            )
                        }
                        ).shuffled(),
                    Filters.notExists("someField"),
                    expectedList,
                )
            },
        )

        @JvmStatic
        fun equalsFilterArguments() = listOf(
            List(positiveRandomInt(10)) { randomChannel(extraData = mapOf("someField" to 2.0)) }
                .let { expectedList ->
                    val doubleValue: Double = 2.0
                    Arguments.of(
                        (expectedList + List(10) { randomChannel() }).shuffled(),
                        Filters.eq("someField", doubleValue),
                        expectedList,
                    )
                },
            List(positiveRandomInt(10)) { randomChannel(extraData = mapOf("someField" to 2.0)) }
                .let { expectedList ->
                    val floatValue: Float = 2F
                    Arguments.of(
                        (expectedList + List(10) { randomChannel() }).shuffled(),
                        Filters.eq("someField", floatValue),
                        expectedList,
                    )
                },
            List(positiveRandomInt(10)) { randomChannel(extraData = mapOf("someField" to 2.0)) }
                .let { expectedList ->
                    val longValue: Long = 2L
                    Arguments.of(
                        (expectedList + List(10) { randomChannel() }).shuffled(),
                        Filters.eq("someField", longValue),
                        expectedList,
                    )
                },
            List(positiveRandomInt(10)) { randomChannel(extraData = mapOf("someField" to 2.0)) }
                .let { expectedList ->
                    val intValue: Int = 2
                    Arguments.of(
                        (expectedList + List(10) { randomChannel() }).shuffled(),
                        Filters.eq("someField", intValue),
                        expectedList,
                    )
                },
            List(positiveRandomInt(10)) { randomChannel(extraData = mapOf("someField" to 2.0)) }
                .let { expectedList ->
                    val charValue: Char = 2.toChar()
                    Arguments.of(
                        (expectedList + List(10) { randomChannel() }).shuffled(),
                        Filters.eq("someField", charValue),
                        expectedList,
                    )
                },
            List(positiveRandomInt(10)) { randomChannel(extraData = mapOf("someField" to 2.0)) }
                .let { expectedList ->
                    val shortValue: Short = 2
                    Arguments.of(
                        (expectedList + List(10) { randomChannel() }).shuffled(),
                        Filters.eq("someField", shortValue),
                        expectedList,
                    )
                },
            List(positiveRandomInt(10)) { randomChannel(extraData = mapOf("someField" to 2.0)) }
                .let { expectedList ->
                    val byteValue: Byte = 2
                    Arguments.of(
                        (expectedList + List(10) { randomChannel() }).shuffled(),
                        Filters.eq("someField", byteValue),
                        expectedList,
                    )
                },
            List(positiveRandomInt(10)) { randomChannel(type = stringQuery) }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(10) { randomChannel(type = randomString(20)) }).shuffled(),
                    Filters.eq("type", stringQuery),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) { randomChannel(watcherCount = intQuery) }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(10) {
                            randomChannel(
                                watcherCount = randomIntBetween(
                                    intQuery + 1,
                                    Int.MAX_VALUE - 1,
                                ),
                            )
                        }
                        ).shuffled(),
                    Filters.eq("watcherCount", intQuery),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) { randomChannel(watcherCount = intQuery) }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(10) {
                            randomChannel(
                                watcherCount = randomIntBetween(
                                    intQuery + 1,
                                    Int.MAX_VALUE - 1,
                                ),
                            )
                        }
                        ).shuffled(),
                    Filters.eq("watcherCount", intQuery),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) { randomChannel(watcherCount = intQuery) }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(10) {
                            randomChannel(
                                watcherCount = randomIntBetween(
                                    Int.MIN_VALUE,
                                    intQuery - 1,
                                ),
                            )
                        }
                        ).shuffled(),
                    Filters.eq("watcherCount", intQuery),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) { randomChannel(watcherCount = intQuery) }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(10) {
                            randomChannel(
                                watcherCount = randomIntBetween(
                                    Int.MIN_VALUE,
                                    intQuery - 1,
                                ),
                            )
                        }
                        ).shuffled(),
                    Filters.eq("watcherCount", intQuery),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf("someField" to longQuery),
                )
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(10) {
                            randomChannel(
                                extraData = mapOf("someField" to randomLongBetween(longQuery + 1, Long.MAX_VALUE - 1)),
                            )
                        }
                        ).shuffled(),
                    Filters.eq("someField", longQuery),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf("someField" to longQuery),
                )
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(10) {
                            randomChannel(
                                extraData = mapOf("someField" to randomLongBetween(longQuery + 1, Long.MAX_VALUE - 1)),
                            )
                        }
                        ).shuffled(),
                    Filters.eq("someField", longQuery),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf("someField" to longQuery),
                )
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(10) {
                            randomChannel(
                                extraData = mapOf("someField" to randomLongBetween(Long.MIN_VALUE, longQuery - 1)),
                            )
                        }
                        ).shuffled(),
                    Filters.eq("someField", longQuery),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf("someField" to longQuery),
                )
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(10) {
                            randomChannel(
                                extraData = mapOf("someField" to randomLongBetween(Long.MIN_VALUE, longQuery - 1)),
                            )
                        }
                        )
                        .shuffled(),
                    Filters.eq("someField", longQuery),
                    expectedList,
                )
            },
        )

        @JvmStatic
        fun notEqualsFilterArguments() = listOf(
            List(10) { randomChannel(type = randomString(20)) }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel(type = stringQuery) }).shuffled(),
                    Filters.ne("type", stringQuery),
                    expectedList,
                )
            },
            List(10) {
                randomChannel(
                    watcherCount = randomIntBetween(
                        intQuery + 1,
                        Int.MAX_VALUE - 1,
                    ),
                )
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel(watcherCount = intQuery) }).shuffled(),
                    Filters.ne("watcherCount", intQuery),
                    expectedList,
                )
            },
            List(10) {
                randomChannel(
                    watcherCount = randomIntBetween(
                        intQuery + 1,
                        Int.MAX_VALUE - 1,
                    ),
                )
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel(watcherCount = intQuery) }).shuffled(),
                    Filters.ne("watcherCount", intQuery),
                    expectedList,
                )
            },
            List(10) {
                randomChannel(
                    watcherCount = randomIntBetween(
                        Int.MIN_VALUE,
                        intQuery - 1,
                    ),
                )
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel(watcherCount = intQuery) }).shuffled(),
                    Filters.ne("watcherCount", intQuery),
                    expectedList,
                )
            },
            List(10) {
                randomChannel(
                    watcherCount = randomIntBetween(
                        Int.MIN_VALUE,
                        intQuery - 1,
                    ),
                )
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel(watcherCount = intQuery) }).shuffled(),
                    Filters.ne("watcherCount", intQuery),
                    expectedList,
                )
            },
            List(10) {
                randomChannel(
                    extraData = mapOf("someField" to randomLongBetween(longQuery + 1, Long.MAX_VALUE - 1)),
                )
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel(
                                extraData = mapOf("someField" to longQuery),
                            )
                        }
                        ).shuffled(),
                    Filters.ne("someField", longQuery),
                    expectedList,
                )
            },
            List(10) {
                randomChannel(
                    extraData = mapOf("someField" to randomLongBetween(longQuery + 1, Long.MAX_VALUE - 1)),
                )
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel(
                                extraData = mapOf("someField" to longQuery),
                            )
                        }
                        ).shuffled(),
                    Filters.ne("someField", longQuery),
                    expectedList,
                )
            },
            List(10) {
                randomChannel(
                    extraData = mapOf("someField" to randomLongBetween(Long.MIN_VALUE, longQuery - 1)),
                )
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel(
                                extraData = mapOf("someField" to longQuery),
                            )
                        }
                        ).shuffled(),
                    Filters.ne("someField", longQuery),
                    expectedList,
                )
            },
            List(10) {
                randomChannel(
                    extraData = mapOf("someField" to randomLongBetween(Long.MIN_VALUE, longQuery - 1)),
                )
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel(
                                extraData = mapOf("someField" to longQuery),
                            )
                        }
                        )
                        .shuffled(),
                    Filters.ne("someField", longQuery),
                    expectedList,
                )
            },
        )

        @JvmStatic
        fun greaterThanFilterArguments() = listOf(
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf("someField" to randomIntBetween(-80, 300)),
                )
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel(
                                extraData = mapOf("someField" to randomIntBetween(Int.MIN_VALUE, -100)),
                            )
                        }
                        ),
                    Filters.greaterThan("someField", randomIntBetween(-100, -81)),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf("someField" to randomLongBetween(-80, 300)),
                )
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel(
                                extraData = mapOf("someField" to randomLongBetween(Long.MIN_VALUE, -100)),
                            )
                        }
                        ),
                    Filters.greaterThan("someField", randomLongBetween(-100, -81)),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf("someField" to "b${randomString()}"),
                )
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel(
                                extraData = mapOf("someField" to "aa${randomString()}"),
                            )
                        }
                        ),
                    Filters.greaterThan("someField", "ab${randomString()}"),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf("someField" to randomIntBetween(-80, 300)),
                )
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.greaterThan("someField", randomIntBetween(-100, -81)),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf("someField" to randomLongBetween(-80, 300)),
                )
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.greaterThan("someField", randomLongBetween(-100, -81)),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf("someField" to "b${randomString()}"),
                )
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.greaterThan("someField", "ab${randomString()}"),
                    expectedList,
                )
            },
        )

        @JvmStatic
        fun greaterThanOrEqualsFilterArguments() = listOf(
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf("someField" to randomIntBetween(-80, 300)),
                )
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel(
                                extraData = mapOf("someField" to randomIntBetween(Int.MIN_VALUE, -100)),
                            )
                        }
                        ),
                    Filters.greaterThanEquals("someField", randomIntBetween(-100, -80)),
                    expectedList,
                )
            },
            (
                List(positiveRandomInt(10)) {
                    randomChannel(
                        extraData = mapOf("someField" to randomIntBetween(-80, 300)),
                    )
                } + randomChannel(
                    extraData = mapOf("someField" to -80),
                )
                ).let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel(
                                extraData = mapOf("someField" to randomIntBetween(Int.MIN_VALUE, -100)),
                            )
                        }
                        ),
                    Filters.greaterThanEquals("someField", -80),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf("someField" to randomLongBetween(-80, 300)),
                )
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel(
                                extraData = mapOf("someField" to randomLongBetween(Long.MIN_VALUE, -100)),
                            )
                        }
                        ),
                    Filters.greaterThanEquals("someField", randomLongBetween(-100, -80)),
                    expectedList,
                )
            },
            (
                List(positiveRandomInt(10)) {
                    randomChannel(
                        extraData = mapOf("someField" to randomLongBetween(-80, 300)),
                    )
                } + randomChannel(
                    extraData = mapOf("someField" to -80L),
                )
                ).let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel(
                                extraData = mapOf("someField" to randomLongBetween(Long.MIN_VALUE, -100)),
                            )
                        }
                        ),
                    Filters.greaterThanEquals("someField", -80L),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf("someField" to "b${randomString()}"),
                )
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel(
                                extraData = mapOf("someField" to "aa${randomString()}"),
                            )
                        }
                        ),
                    Filters.greaterThanEquals("someField", "ab${randomString()}"),
                    expectedList,
                )
            },
            (
                List(positiveRandomInt(10)) {
                    randomChannel(
                        extraData = mapOf("someField" to "b${randomString()}"),
                    )
                } + randomChannel(
                    extraData = mapOf("someField" to "ab"),
                )
                ).let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel(
                                extraData = mapOf("someField" to "aa${randomString()}"),
                            )
                        }
                        ),
                    Filters.greaterThanEquals("someField", "ab"),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf("someField" to randomIntBetween(-80, 300)),
                )
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.greaterThanEquals("someField", randomIntBetween(-100, -80)),
                    expectedList,
                )
            },
            (
                List(positiveRandomInt(10)) {
                    randomChannel(
                        extraData = mapOf("someField" to randomIntBetween(-80, 300)),
                    )
                } + randomChannel(
                    extraData = mapOf("someField" to -80),
                )
                ).let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.greaterThanEquals("someField", -80),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf("someField" to randomLongBetween(-80, 300)),
                )
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.greaterThanEquals("someField", randomLongBetween(-100, -80)),
                    expectedList,
                )
            },
            (
                List(positiveRandomInt(10)) {
                    randomChannel(
                        extraData = mapOf("someField" to randomLongBetween(-80, 300)),
                    )
                } + randomChannel(
                    extraData = mapOf("someField" to -80L),
                )
                ).let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.greaterThanEquals("someField", -80L),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf("someField" to "b${randomString()}"),
                )
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.greaterThanEquals("someField", "ab${randomString()}"),
                    expectedList,
                )
            },
            (
                List(positiveRandomInt(10)) {
                    randomChannel(
                        extraData = mapOf("someField" to "b${randomString()}"),
                    )
                } + randomChannel(
                    extraData = mapOf("someField" to "ab"),
                )
                ).let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.greaterThanEquals("someField", "ab"),
                    expectedList,
                )
            },
        )

        @JvmStatic
        fun lessThanFilterArguments() = listOf(
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf("someField" to randomIntBetween(-80, 300)),
                )
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel(
                                extraData = mapOf("someField" to randomIntBetween(320, Int.MAX_VALUE - 1)),
                            )
                        }
                        ),
                    Filters.lessThan("someField", randomIntBetween(301, 320)),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf("someField" to randomLongBetween(-80, 300)),
                )
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel(
                                extraData = mapOf("someField" to randomLongBetween(320, Long.MAX_VALUE - 1)),
                            )
                        }
                        ),
                    Filters.lessThan("someField", randomLongBetween(301, 320)),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf("someField" to "a${randomString()}"),
                )
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel(
                                extraData = mapOf("someField" to "bb${randomString()}"),
                            )
                        }
                        ),
                    Filters.lessThan("someField", "ba${randomString()}"),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf("someField" to randomIntBetween(-80, 300)),
                )
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.lessThan("someField", randomIntBetween(301, 320)),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf("someField" to randomLongBetween(-80, 300)),
                )
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.lessThan("someField", randomLongBetween(301, 320)),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf("someField" to "a${randomString()}"),
                )
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.lessThan("someField", "ba${randomString()}"),
                    expectedList,
                )
            },
        )

        @JvmStatic
        fun lessThanOrEqualsFilterArguments() = listOf(
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf("someField" to randomIntBetween(-80, 300)),
                )
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel(
                                extraData = mapOf("someField" to randomIntBetween(320, Int.MAX_VALUE - 1)),
                            )
                        }
                        ),
                    Filters.lessThanEquals("someField", randomIntBetween(300, 320)),
                    expectedList,
                )
            },
            (
                List(positiveRandomInt(10)) {
                    randomChannel(
                        extraData = mapOf("someField" to randomIntBetween(-80, 300)),
                    )
                } + randomChannel(
                    extraData = mapOf("someField" to 300),
                )
                ).let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel(
                                extraData = mapOf("someField" to randomIntBetween(320, Int.MAX_VALUE - 1)),
                            )
                        }
                        ),
                    Filters.lessThanEquals("someField", 300),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf("someField" to randomLongBetween(-80, 300)),
                )
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel(
                                extraData = mapOf("someField" to randomLongBetween(320, Long.MAX_VALUE - 1)),
                            )
                        }
                        ),
                    Filters.lessThanEquals("someField", randomLongBetween(300, 320)),
                    expectedList,
                )
            },
            (
                List(positiveRandomInt(10)) {
                    randomChannel(
                        extraData = mapOf("someField" to randomLongBetween(-80, 300)),
                    )
                } + randomChannel(
                    extraData = mapOf("someField" to 300L),
                )
                ).let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel(
                                extraData = mapOf("someField" to randomLongBetween(320, Long.MAX_VALUE - 1)),
                            )
                        }
                        ),
                    Filters.lessThanEquals("someField", 300L),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf("someField" to "a${randomString()}"),
                )
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel(
                                extraData = mapOf("someField" to "bb${randomString()}"),
                            )
                        }
                        ),
                    Filters.lessThanEquals("someField", "ba${randomString()}"),
                    expectedList,
                )
            },
            (
                List(positiveRandomInt(10)) {
                    randomChannel(
                        extraData = mapOf("someField" to "a${randomString()}"),
                    )
                } + randomChannel(
                    extraData = mapOf("someField" to "ba"),
                )
                ).let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel(
                                extraData = mapOf("someField" to "bb${randomString()}"),
                            )
                        }
                        ),
                    Filters.lessThanEquals("someField", "ba"),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf("someField" to randomIntBetween(-80, 300)),
                )
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.lessThanEquals("someField", randomIntBetween(300, 320)),
                    expectedList,
                )
            },
            (
                List(positiveRandomInt(10)) {
                    randomChannel(
                        extraData = mapOf("someField" to randomIntBetween(-80, 300)),
                    )
                } + randomChannel(
                    extraData = mapOf("someField" to 300),
                )
                ).let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.lessThanEquals("someField", 300),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf("someField" to randomLongBetween(-80, 300)),
                )
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.lessThanEquals("someField", randomLongBetween(300, 320)),
                    expectedList,
                )
            },
            (
                List(positiveRandomInt(10)) {
                    randomChannel(
                        extraData = mapOf("someField" to randomLongBetween(-80, 300)),
                    )
                } + randomChannel(
                    extraData = mapOf("someField" to 300L),
                )
                ).let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.lessThanEquals("someField", 300L),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel(
                    extraData = mapOf("someField" to "a${randomString()}"),
                )
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.lessThanEquals("someField", "ba${randomString()}"),
                    expectedList,
                )
            },
            (
                List(positiveRandomInt(10)) {
                    randomChannel(
                        extraData = mapOf("someField" to "a${randomString()}"),
                    )
                } + randomChannel(
                    extraData = mapOf("someField" to "ba"),
                )
                ).let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.lessThanEquals("someField", "ba"),
                    expectedList,
                )
            },
        )

        @JvmStatic
        fun inFilterArguments() = List(positiveRandomInt(10)) { randomInt() }.let { intList ->
            val notIntList = List(positiveRandomInt(10)) { randomInt() } - intList
            listOf(
                List(positiveRandomInt(10)) {
                    randomChannel(
                        extraData = mapOf("someField" to intList.random()),
                    )
                }.let { expectedList ->
                    Arguments.of(
                        (
                            expectedList + List(positiveRandomInt(10)) {
                                randomChannel(
                                    extraData = mapOf("someField" to notIntList.random()),
                                )
                            }
                            ).shuffled(),
                        Filters.`in`("someField", intList),
                        expectedList,
                    )
                },
                List(positiveRandomInt(10)) {
                    randomChannel(
                        extraData = mapOf("someField" to intList.random()),
                    )
                }.let { expectedList ->
                    Arguments.of(
                        (expectedList + List(positiveRandomInt(10)) { randomChannel() }).shuffled(),
                        Filters.`in`("someField", intList),
                        expectedList,
                    )
                },
            )
        } +
            List(positiveRandomInt(10)) { randomLong() }.let { longList ->
                val notLongList = List(positiveRandomInt(10)) { randomLong() } - longList
                listOf(
                    List(positiveRandomInt(10)) {
                        randomChannel(
                            extraData = mapOf("someField" to longList.random()),
                        )
                    }.let { expectedList ->
                        Arguments.of(
                            (
                                expectedList + List(positiveRandomInt(10)) {
                                    randomChannel(
                                        extraData = mapOf("someField" to notLongList.random()),
                                    )
                                }
                                ).shuffled(),
                            Filters.`in`("someField", longList),
                            expectedList,
                        )
                    },
                    List(positiveRandomInt(10)) {
                        randomChannel(
                            extraData = mapOf("someField" to longList.random()),
                        )
                    }.let { expectedList ->
                        Arguments.of(
                            (expectedList + List(positiveRandomInt(10)) { randomChannel() }).shuffled(),
                            Filters.`in`("someField", longList),
                            expectedList,
                        )
                    },
                )
            } +
            List(positiveRandomInt(10)) { randomString() }.let { stringList ->
                val notStringList = List(positiveRandomInt(10)) { randomString() } - stringList
                listOf(
                    List(positiveRandomInt(10)) {
                        randomChannel(
                            extraData = mapOf("someField" to stringList.random()),
                        )
                    }.let { expectedList ->
                        Arguments.of(
                            (
                                expectedList + List(positiveRandomInt(10)) {
                                    randomChannel(
                                        extraData = mapOf("someField" to notStringList.random()),
                                    )
                                }
                                ).shuffled(),
                            Filters.`in`("someField", stringList),
                            expectedList,
                        )
                    },
                    List(positiveRandomInt(10)) {
                        randomChannel(
                            extraData = mapOf("someField" to stringList.random()),
                        )
                    }.let { expectedList ->
                        Arguments.of(
                            (expectedList + List(positiveRandomInt(10)) { randomChannel() }).shuffled(),
                            Filters.`in`("someField", stringList),
                            expectedList,
                        )
                    },
                )
            } +
            memberIds.take(positiveRandomInt(memberIds.size)).map { randomMember(user = randomUser(id = it)) }
                .let { members ->
                    List(positiveRandomInt(10)) {
                        randomChannel(
                            members = (List(positiveRandomInt(10)) { randomMember() } + members).shuffled(),
                        )
                    }.let { expectedList ->
                        Arguments.of(
                            (expectedList + List(positiveRandomInt(10)) { randomChannel() }).shuffled(),
                            Filters.`in`("members", memberIds),
                            expectedList,
                        )
                    }
                } +
            List(positiveRandomInt(10)) { randomString() }.let { stringList ->
                val notStringList = List(positiveRandomInt(10)) { randomString() } - stringList
                listOf(
                    List(positiveRandomInt(10)) {
                        randomChannel(
                            extraData = mapOf("someField" to listOf(stringList.random(), notStringList.random())),
                        )
                    }.let { expectedList ->
                        Arguments.of(
                            (
                                expectedList + List(positiveRandomInt(10)) {
                                    randomChannel(
                                        extraData = mapOf("someField" to listOf(notStringList.random())),
                                    )
                                }
                                ).shuffled(),
                            Filters.`in`("someField", stringList),
                            expectedList,
                        )
                    },
                    List(positiveRandomInt(10)) {
                        randomChannel(
                            extraData = mapOf("someField" to listOf(stringList.random(), notStringList.random())),
                        )
                    }.let { expectedList ->
                        Arguments.of(
                            (expectedList + List(positiveRandomInt(10)) { randomChannel() }).shuffled(),
                            Filters.`in`("someField", stringList),
                            expectedList,
                        )
                    },
                )
            }

        @Suppress("LongMethod")
        @JvmStatic
        fun notInFilterArguments() = List(positiveRandomInt(10)) { randomInt() }.let { intList ->
            val notIntList = List(positiveRandomInt(10)) { randomInt() } - intList
            listOf(
                List(positiveRandomInt(10)) {
                    randomChannel(
                        extraData = mapOf("someField" to notIntList.random()),
                    )
                }.let { expectedList ->
                    Arguments.of(
                        (
                            expectedList + List(positiveRandomInt(10)) {
                                randomChannel(
                                    extraData = mapOf("someField" to intList.random()),
                                )
                            }
                            ).shuffled(),
                        Filters.nin("someField", intList),
                        expectedList,
                    )
                },
                List(positiveRandomInt(10)) { randomChannel() }.let { expectedList ->
                    Arguments.of(
                        (
                            expectedList + List(positiveRandomInt(10)) {
                                randomChannel(
                                    extraData = mapOf("someField" to intList.random()),
                                )
                            }
                            ).shuffled(),
                        Filters.nin("someField", intList),
                        expectedList,
                    )
                },
            )
        } +
            List(positiveRandomInt(10)) { randomLong() }.let { longList ->
                val notLongList = List(positiveRandomInt(10)) { randomLong() } - longList
                listOf(
                    List(positiveRandomInt(10)) {
                        randomChannel(
                            extraData = mapOf("someField" to notLongList.random()),
                        )
                    }.let { expectedList ->
                        Arguments.of(
                            (
                                expectedList + List(positiveRandomInt(10)) {
                                    randomChannel(
                                        extraData = mapOf("someField" to longList.random()),
                                    )
                                }
                                ).shuffled(),
                            Filters.nin("someField", longList),
                            expectedList,
                        )
                    },
                    List(positiveRandomInt(10)) { randomChannel() }.let { expectedList ->
                        Arguments.of(
                            (
                                expectedList + List(positiveRandomInt(10)) {
                                    randomChannel(
                                        extraData = mapOf("someField" to longList.random()),
                                    )
                                }
                                ).shuffled(),
                            Filters.nin("someField", longList),
                            expectedList,
                        )
                    },
                )
            } +
            List(positiveRandomInt(10)) { randomString() }.let { stringList ->
                val notStringList = List(positiveRandomInt(10)) { randomString() } - stringList
                listOf(
                    List(positiveRandomInt(10)) {
                        randomChannel(
                            extraData = mapOf("someField" to notStringList.random()),
                        )
                    }.let { expectedList ->
                        Arguments.of(
                            (
                                expectedList + List(positiveRandomInt(10)) {
                                    randomChannel(
                                        extraData = mapOf("someField" to stringList.random()),
                                    )
                                }
                                ).shuffled(),
                            Filters.nin("someField", stringList),
                            expectedList,
                        )
                    },
                    List(positiveRandomInt(10)) { randomChannel() }.let { expectedList ->
                        Arguments.of(
                            (
                                expectedList + List(positiveRandomInt(10)) {
                                    randomChannel(
                                        extraData = mapOf("someField" to stringList.random()),
                                    )
                                }
                                ).shuffled(),
                            Filters.nin("someField", stringList),
                            expectedList,
                        )
                    },
                )
            } +
            List(positiveRandomInt(10)) { randomChannel() }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList +
                            memberIds.take(positiveRandomInt(memberIds.size))
                                .map { randomMember(user = randomUser(id = it)) }.let { members ->
                                    List(positiveRandomInt(10)) {
                                        randomChannel(
                                            members = (
                                                List(positiveRandomInt(10)) { randomMember() } + members
                                                ).shuffled(),
                                        )
                                    }
                                }
                        ).shuffled(),
                    Filters.nin("members", memberIds),
                    expectedList,
                )
            } +
            List(positiveRandomInt(10)) { randomString() }.let { stringList ->
                val notStringList = List(positiveRandomInt(10)) { randomString() } - stringList
                listOf(
                    List(positiveRandomInt(10)) {
                        randomChannel(
                            extraData = mapOf("someField" to listOf(notStringList.random())),
                        )
                    }.let { expectedList ->
                        Arguments.of(
                            (
                                expectedList + List(positiveRandomInt(10)) {
                                    randomChannel(
                                        extraData = mapOf("someField" to listOf(stringList.random())),
                                    )
                                }
                                ).shuffled(),
                            Filters.nin("someField", stringList),
                            expectedList,
                        )
                    },
                    List(positiveRandomInt(10)) { randomChannel() }.let { expectedList ->
                        Arguments.of(
                            (
                                expectedList + List(positiveRandomInt(10)) {
                                    randomChannel(
                                        extraData = mapOf("someField" to listOf(stringList.random())),
                                    )
                                }
                                ).shuffled(),
                            Filters.nin("someField", stringList),
                            expectedList,
                        )
                    },
                )
            }

        @JvmStatic
        fun andFilterArguments() = List(positiveRandomInt(10)) {
            randomChannel(
                syncStatus = SyncStatus.FAILED_PERMANENTLY,
                type = "1${randomString()}",
                extraData = mapOf("Something" to listOf(1, 2, 3)),
            )
        }.let { expectedList ->
            listOf(
                Arguments.of(
                    (expectedList + List(10) { randomChannel() }).shuffled(),
                    Filters.and(
                        Filters.greaterThan("syncStatus", SyncStatus.COMPLETED),
                        Filters.greaterThan("type", "0"),
                        Filters.contains("Something", 2),
                    ),
                    expectedList,
                ),
            )
        }

        @JvmStatic
        fun norFilterArguments() = List(positiveRandomInt(10)) {
            randomChannel(
                syncStatus = randomSyncStatus(
                    listOf(SyncStatus.FAILED_PERMANENTLY, SyncStatus.IN_PROGRESS),
                ),
                type = "a${randomString()}",
            )
        }.let { expectedList ->
            listOf(
                Arguments.of(
                    (
                        expectedList + List(10) {
                            randomChannel(
                                syncStatus = SyncStatus.FAILED_PERMANENTLY,
                                type = "c${randomString()}",
                                extraData = mapOf("Something" to listOf(1, 2, 3)),
                            )
                        }
                        ).shuffled(),
                    Filters.nor(
                        Filters.greaterThan("syncStatus", SyncStatus.COMPLETED),
                        Filters.greaterThan("type", "b"),
                        Filters.contains("Something", 2),
                    ),
                    expectedList,
                ),
                Arguments.of(
                    expectedList,
                    Filters.nor(
                        Filters.greaterThan("syncStatus", SyncStatus.COMPLETED),
                        Filters.greaterThan("type", "b"),
                        Filters.contains("Something", 2),
                        NeutralFilterObject,
                    ),
                    emptyList<Channel>(),
                ),
            )
        }

        @JvmStatic
        fun orFilterArguments() = List(positiveRandomInt(10)) {
            randomChannel(
                syncStatus = randomSyncStatus(
                    listOf(SyncStatus.SYNC_NEEDED, SyncStatus.COMPLETED),
                ),
                type = "b${randomString()}",
                extraData = mapOf("Something" to listOf(1, 2, 3)),
            )
        }.let { expectedList ->
            listOf(
                Arguments.of(
                    (
                        expectedList + List(10) {
                            randomChannel(
                                syncStatus = randomSyncStatus(
                                    listOf(
                                        SyncStatus.FAILED_PERMANENTLY,
                                        SyncStatus.IN_PROGRESS,
                                    ),
                                ),
                                type = "a${randomString()}",
                            )
                        }
                        ).shuffled(),
                    Filters.or(
                        Filters.greaterThan("syncStatus", SyncStatus.COMPLETED),
                        Filters.greaterThan("type", "b"),
                        Filters.contains("Something", 2),
                    ),
                    expectedList,
                ),
                (expectedList + List(10) { randomChannel() }).shuffled().let {
                    Arguments.of(
                        it,
                        Filters.or(
                            Filters.greaterThan("syncStatus", SyncStatus.COMPLETED),
                            Filters.greaterThan("type", "0"),
                            Filters.contains("Something", 2),
                            NeutralFilterObject,
                        ),
                        it,
                    )
                },
            )
        }
    }
}
