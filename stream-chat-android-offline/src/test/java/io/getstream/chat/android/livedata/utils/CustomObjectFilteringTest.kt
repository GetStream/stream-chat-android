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
            containsFilterArguments() +
            autocompleteFilterArguments() +
            existsFilterArguments() +
            notExistsFilterArguments() +
            equalsFilterArguments() +
            notEqualsFilterArguments() +
            greaterThanFilterArguments() +
            greaterThanOrEqualsFilterArguments() +
            lessThanFilterArguments() +
            lessThanOrEqualsFilterArguments()

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

        @JvmStatic
        fun autocompleteFilterArguments() = listOf(
            List(positiveRandomInt(10)) { randomChannel(type = randomString(20) + stringQuery + randomString(20)) }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(10) { randomChannel(type = randomString(8)) }).shuffled(),
                    Filters.autocomplete("type", stringQuery),
                    expectedList,
                )
            }
        )

        @JvmStatic
        fun existsFilterArguments() = listOf(
            List(positiveRandomInt(10)) {
                randomChannel().apply {
                    extraData["someField"] = longQuery
                }
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(10) { randomChannel() }).shuffled(),
                    Filters.exists("someField"),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel().apply {
                    extraData["someField"] = stringQuery
                }
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(10) { randomChannel() }).shuffled(),
                    Filters.exists("someField"),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel().apply {
                    extraData["someField"] = intQuery
                }
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
                            randomChannel().apply {
                                extraData["someField"] = longQuery
                            }
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
                            randomChannel().apply {
                                extraData["someField"] = stringQuery
                            }
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
                            randomChannel().apply {
                                extraData["someField"] = intQuery
                            }
                        }
                        ).shuffled(),
                    Filters.notExists("someField"),
                    expectedList,
                )
            },
        )

        @JvmStatic
        fun equalsFilterArguments() = listOf(
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
                                    Int.MAX_VALUE - 1
                                )
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
                                    Int.MAX_VALUE - 1
                                )
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
                                    intQuery - 1
                                )
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
                                    intQuery - 1
                                )
                            )
                        }
                        ).shuffled(),
                    Filters.eq("watcherCount", intQuery),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel().apply {
                    extraData["someField"] = longQuery
                }
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(10) {
                            randomChannel().apply {
                                extraData["someField"] = randomLongBetween(longQuery + 1, Long.MAX_VALUE - 1)
                            }
                        }
                        ).shuffled(),
                    Filters.eq("someField", longQuery),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel().apply {
                    extraData["someField"] = longQuery
                }
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(10) {
                            randomChannel().apply {
                                extraData["someField"] = randomLongBetween(longQuery + 1, Long.MAX_VALUE - 1)
                            }
                        }
                        ).shuffled(),
                    Filters.eq("someField", longQuery),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel().apply {
                    extraData["someField"] = longQuery
                }
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(10) {
                            randomChannel().apply {
                                extraData["someField"] = randomLongBetween(Long.MIN_VALUE, longQuery - 1)
                            }
                        }
                        ).shuffled(),
                    Filters.eq("someField", longQuery),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel().apply {
                    extraData["someField"] = longQuery
                }
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(10) {
                            randomChannel().apply {
                                extraData["someField"] = randomLongBetween(Long.MIN_VALUE, longQuery - 1)
                            }
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
                        Int.MAX_VALUE - 1
                    )
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
                        Int.MAX_VALUE - 1
                    )
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
                        intQuery - 1
                    )
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
                        intQuery - 1
                    )
                )
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel(watcherCount = intQuery) }).shuffled(),
                    Filters.ne("watcherCount", intQuery),
                    expectedList,
                )
            },
            List(10) {
                randomChannel().apply {
                    extraData["someField"] = randomLongBetween(longQuery + 1, Long.MAX_VALUE - 1)
                }
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel().apply {
                                extraData["someField"] = longQuery
                            }
                        }
                        ).shuffled(),
                    Filters.ne("someField", longQuery),
                    expectedList,
                )
            },
            List(10) {
                randomChannel().apply {
                    extraData["someField"] = randomLongBetween(longQuery + 1, Long.MAX_VALUE - 1)
                }
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel().apply {
                                extraData["someField"] = longQuery
                            }
                        }
                        ).shuffled(),
                    Filters.ne("someField", longQuery),
                    expectedList,
                )
            },
            List(10) {
                randomChannel().apply {
                    extraData["someField"] = randomLongBetween(Long.MIN_VALUE, longQuery - 1)
                }
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel().apply {
                                extraData["someField"] = longQuery
                            }
                        }
                        ).shuffled(),
                    Filters.ne("someField", longQuery),
                    expectedList,
                )
            },
            List(10) {
                randomChannel().apply {
                    extraData["someField"] = randomLongBetween(Long.MIN_VALUE, longQuery - 1)
                }
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel().apply {
                                extraData["someField"] = longQuery
                            }
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
                randomChannel().apply {
                    extraData["someField"] = randomIntBetween(-80, 300)
                }
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel().apply {
                                extraData["someField"] = randomIntBetween(Int.MIN_VALUE, -100)
                            }
                        }
                        ),
                    Filters.greaterThan("someField", randomIntBetween(-100, -80)),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel().apply {
                    extraData["someField"] = randomLongBetween(-80, 300)
                }
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel().apply {
                                extraData["someField"] = randomLongBetween(Long.MIN_VALUE, -100)
                            }
                        }
                        ),
                    Filters.greaterThan("someField", randomLongBetween(-100, -80)),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel().apply {
                    extraData["someField"] = "b${randomString()}"
                }
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel().apply {
                                extraData["someField"] = "aa${randomString()}"
                            }
                        }
                        ),
                    Filters.greaterThan("someField", "ab${randomString()}"),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel().apply {
                    extraData["someField"] = randomIntBetween(-80, 300)
                }
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.greaterThan("someField", randomIntBetween(-100, -80)),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel().apply {
                    extraData["someField"] = randomLongBetween(-80, 300)
                }
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.greaterThan("someField", randomLongBetween(-100, -80)),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel().apply {
                    extraData["someField"] = "b${randomString()}"
                }
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
                randomChannel().apply {
                    extraData["someField"] = randomIntBetween(-80, 300)
                }
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel().apply {
                                extraData["someField"] = randomIntBetween(Int.MIN_VALUE, -100)
                            }
                        }
                        ),
                    Filters.greaterThanEquals("someField", randomIntBetween(-100, -80)),
                    expectedList,
                )
            },
            (
                List(positiveRandomInt(10)) {
                    randomChannel().apply {
                        extraData["someField"] = randomIntBetween(-80, 300)
                    }
                } + randomChannel().apply {
                    extraData["someField"] = -80
                }
                ).let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel().apply {
                                extraData["someField"] = randomIntBetween(Int.MIN_VALUE, -100)
                            }
                        }
                        ),
                    Filters.greaterThanEquals("someField", -80),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel().apply {
                    extraData["someField"] = randomLongBetween(-80, 300)
                }
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel().apply {
                                extraData["someField"] = randomLongBetween(Long.MIN_VALUE, -100)
                            }
                        }
                        ),
                    Filters.greaterThanEquals("someField", randomLongBetween(-100, -80)),
                    expectedList,
                )
            },
            (
                List(positiveRandomInt(10)) {
                    randomChannel().apply {
                        extraData["someField"] = randomLongBetween(-80, 300)
                    }
                } + randomChannel().apply {
                    extraData["someField"] = -80L
                }
                ).let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel().apply {
                                extraData["someField"] = randomLongBetween(Long.MIN_VALUE, -100)
                            }
                        }
                        ),
                    Filters.greaterThanEquals("someField", -80L),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel().apply {
                    extraData["someField"] = "b${randomString()}"
                }
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel().apply {
                                extraData["someField"] = "aa${randomString()}"
                            }
                        }
                        ),
                    Filters.greaterThanEquals("someField", "ab${randomString()}"),
                    expectedList,
                )
            },
            (
                List(positiveRandomInt(10)) {
                    randomChannel().apply {
                        extraData["someField"] = "b${randomString()}"
                    }
                } + randomChannel().apply {
                    extraData["someField"] = "ab"
                }
                ).let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel().apply {
                                extraData["someField"] = "aa${randomString()}"
                            }
                        }
                        ),
                    Filters.greaterThanEquals("someField", "ab"),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel().apply {
                    extraData["someField"] = randomIntBetween(-80, 300)
                }
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.greaterThanEquals("someField", randomIntBetween(-100, -80)),
                    expectedList,
                )
            },
            (
                List(positiveRandomInt(10)) {
                    randomChannel().apply {
                        extraData["someField"] = randomIntBetween(-80, 300)
                    }
                } + randomChannel().apply {
                    extraData["someField"] = -80
                }
                ).let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.greaterThanEquals("someField", -80),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel().apply {
                    extraData["someField"] = randomLongBetween(-80, 300)
                }
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.greaterThanEquals("someField", randomLongBetween(-100, -80)),
                    expectedList,
                )
            },
            (
                List(positiveRandomInt(10)) {
                    randomChannel().apply {
                        extraData["someField"] = randomLongBetween(-80, 300)
                    }
                } + randomChannel().apply {
                    extraData["someField"] = -80L
                }
                ).let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.greaterThanEquals("someField", -80L),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel().apply {
                    extraData["someField"] = "b${randomString()}"
                }
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.greaterThanEquals("someField", "ab${randomString()}"),
                    expectedList,
                )
            },
            (
                List(positiveRandomInt(10)) {
                    randomChannel().apply {
                        extraData["someField"] = "b${randomString()}"
                    }
                } + randomChannel().apply {
                    extraData["someField"] = "ab"
                }
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
                randomChannel().apply {
                    extraData["someField"] = randomIntBetween(-80, 300)
                }
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel().apply {
                                extraData["someField"] = randomIntBetween(320, Int.MAX_VALUE - 1)
                            }
                        }
                        ),
                    Filters.lessThan("someField", randomIntBetween(300, 320)),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel().apply {
                    extraData["someField"] = randomLongBetween(-80, 300)
                }
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel().apply {
                                extraData["someField"] = randomLongBetween(320, Long.MAX_VALUE - 1)
                            }
                        }
                        ),
                    Filters.lessThan("someField", randomLongBetween(300, 320)),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel().apply {
                    extraData["someField"] = "a${randomString()}"
                }
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel().apply {
                                extraData["someField"] = "bb${randomString()}"
                            }
                        }
                        ),
                    Filters.lessThan("someField", "ba${randomString()}"),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel().apply {
                    extraData["someField"] = randomIntBetween(-80, 300)
                }
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.lessThan("someField", randomIntBetween(300, 320)),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel().apply {
                    extraData["someField"] = randomLongBetween(-80, 300)
                }
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.lessThan("someField", randomLongBetween(300, 320)),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel().apply {
                    extraData["someField"] = "a${randomString()}"
                }
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
                randomChannel().apply {
                    extraData["someField"] = randomIntBetween(-80, 300)
                }
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel().apply {
                                extraData["someField"] = randomIntBetween(320, Int.MAX_VALUE - 1)
                            }
                        }
                        ),
                    Filters.lessThanEquals("someField", randomIntBetween(300, 320)),
                    expectedList,
                )
            },
            (
                List(positiveRandomInt(10)) {
                    randomChannel().apply {
                        extraData["someField"] = randomIntBetween(-80, 300)
                    }
                } + randomChannel().apply {
                    extraData["someField"] = 300
                }
                ).let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel().apply {
                                extraData["someField"] = randomIntBetween(320, Int.MAX_VALUE - 1)
                            }
                        }
                        ),
                    Filters.lessThanEquals("someField", 300),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel().apply {
                    extraData["someField"] = randomLongBetween(-80, 300)
                }
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel().apply {
                                extraData["someField"] = randomLongBetween(320, Long.MAX_VALUE - 1)
                            }
                        }
                        ),
                    Filters.lessThanEquals("someField", randomLongBetween(300, 320)),
                    expectedList,
                )
            },
            (
                List(positiveRandomInt(10)) {
                    randomChannel().apply {
                        extraData["someField"] = randomLongBetween(-80, 300)
                    }
                } + randomChannel().apply {
                    extraData["someField"] = 300L
                }
                ).let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel().apply {
                                extraData["someField"] = randomLongBetween(320, Long.MAX_VALUE - 1)
                            }
                        }
                        ),
                    Filters.lessThanEquals("someField", 300L),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel().apply {
                    extraData["someField"] = "a${randomString()}"
                }
            }.let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel().apply {
                                extraData["someField"] = "bb${randomString()}"
                            }
                        }
                        ),
                    Filters.lessThanEquals("someField", "ba${randomString()}"),
                    expectedList,
                )
            },
            (
                List(positiveRandomInt(10)) {
                    randomChannel().apply {
                        extraData["someField"] = "a${randomString()}"
                    }
                } + randomChannel().apply {
                    extraData["someField"] = "ba"
                }
                ).let { expectedList ->
                Arguments.of(
                    (
                        expectedList + List(positiveRandomInt(10)) {
                            randomChannel().apply {
                                extraData["someField"] = "bb${randomString()}"
                            }
                        }
                        ),
                    Filters.lessThanEquals("someField", "ba"),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel().apply {
                    extraData["someField"] = randomIntBetween(-80, 300)
                }
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.lessThanEquals("someField", randomIntBetween(300, 320)),
                    expectedList,
                )
            },
            (
                List(positiveRandomInt(10)) {
                    randomChannel().apply {
                        extraData["someField"] = randomIntBetween(-80, 300)
                    }
                } + randomChannel().apply {
                    extraData["someField"] = 300
                }
                ).let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.lessThanEquals("someField", 300),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel().apply {
                    extraData["someField"] = randomLongBetween(-80, 300)
                }
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.lessThanEquals("someField", randomLongBetween(300, 320)),
                    expectedList,
                )
            },
            (
                List(positiveRandomInt(10)) {
                    randomChannel().apply {
                        extraData["someField"] = randomLongBetween(-80, 300)
                    }
                } + randomChannel().apply {
                    extraData["someField"] = 300L
                }
                ).let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.lessThanEquals("someField", 300L),
                    expectedList,
                )
            },
            List(positiveRandomInt(10)) {
                randomChannel().apply {
                    extraData["someField"] = "a${randomString()}"
                }
            }.let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.lessThanEquals("someField", "ba${randomString()}"),
                    expectedList,
                )
            },
            (
                List(positiveRandomInt(10)) {
                    randomChannel().apply {
                        extraData["someField"] = "a${randomString()}"
                    }
                } + randomChannel().apply {
                    extraData["someField"] = "ba"
                }
                ).let { expectedList ->
                Arguments.of(
                    (expectedList + List(positiveRandomInt(10)) { randomChannel() }),
                    Filters.lessThanEquals("someField", "ba"),
                    expectedList,
                )
            },
        )
    }
}
