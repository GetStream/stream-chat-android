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

package io.getstream.chat.android.client.internal.offline.repository.database.converter

import io.getstream.chat.android.client.internal.offline.repository.database.converter.FilterObjectConverterTest.Companion.objectToStringArguments
import io.getstream.chat.android.client.internal.offline.repository.database.converter.FilterObjectConverterTest.Companion.stringToObjectArguments
import io.getstream.chat.android.client.internal.offline.repository.database.converter.internal.FilterObjectConverter
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomString
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class FilterObjectConverterTest {

    private val filterObjectConverter = FilterObjectConverter()

    /** [objectToStringArguments] */
    @ParameterizedTest
    @MethodSource("objectToStringArguments")
    fun objectToStringTest(filterObject: FilterObject, expectedJson: String) {
        val result = filterObjectConverter.objectToString(filterObject)

        result shouldBeEqualTo expectedJson
    }

    /** [stringToObjectArguments] */
    @ParameterizedTest
    @MethodSource("stringToObjectArguments")
    fun stringToObjectTest(json: String, expectedFilterObject: FilterObject) {
        val result = filterObjectConverter.stringToObject(json)

        result shouldBeEqualTo expectedFilterObject
    }

    companion object {

        @Suppress("LongMethod")
        @JvmStatic
        fun objectToStringArguments() = listOf(
            Arguments.of(Filters.neutral(), "{}"),
            randomString().let { Arguments.of(Filters.exists(it), "{\"$it\":{\"\$exists\":true}}") },
            randomString().let { Arguments.of(Filters.notExists(it), "{\"$it\":{\"\$exists\":false}}") },
            randomString().let { fieldName ->
                randomString().let { value ->
                    Arguments.of(
                        Filters.autocomplete(fieldName, value),
                        "{\"$fieldName\":{\"\$autocomplete\":\"$value\"}}",
                    )
                }
            },
            randomString().let { fieldName ->
                randomString().let { value ->
                    Arguments.of(Filters.eq(fieldName, value), "{\"$fieldName\":{\"\$eq\":\"$value\"}}")
                }
            },
            randomString().let { fieldName ->
                randomInt().let { value ->
                    Arguments.of(Filters.eq(fieldName, value), "{\"$fieldName\":{\"\$eq\":$value}}")
                }
            },
            randomString().let { fieldName ->
                randomBoolean().let { value ->
                    Arguments.of(Filters.eq(fieldName, value), "{\"$fieldName\":{\"\$eq\":$value}}")
                }
            },
            randomString().let { fieldName ->
                randomString().let { value ->
                    Arguments.of(Filters.ne(fieldName, value), "{\"$fieldName\":{\"\$ne\":\"$value\"}}")
                }
            },
            randomString().let { fieldName ->
                randomInt().let { value ->
                    Arguments.of(Filters.ne(fieldName, value), "{\"$fieldName\":{\"\$ne\":$value}}")
                }
            },
            randomString().let { fieldName ->
                randomBoolean().let { value ->
                    Arguments.of(Filters.ne(fieldName, value), "{\"$fieldName\":{\"\$ne\":$value}}")
                }
            },
            randomString().let { fieldName ->
                randomString().let { value ->
                    Arguments.of(Filters.contains(fieldName, value), "{\"$fieldName\":{\"\$contains\":\"$value\"}}")
                }
            },
            randomString().let { fieldName ->
                randomInt().let { value ->
                    Arguments.of(Filters.contains(fieldName, value), "{\"$fieldName\":{\"\$contains\":$value}}")
                }
            },
            randomString().let { fieldName ->
                randomBoolean().let { value ->
                    Arguments.of(Filters.contains(fieldName, value), "{\"$fieldName\":{\"\$contains\":$value}}")
                }
            },
            randomString().let { fieldName ->
                randomString().let { value ->
                    Arguments.of(Filters.greaterThan(fieldName, value), "{\"$fieldName\":{\"\$gt\":\"$value\"}}")
                }
            },
            randomString().let { fieldName ->
                randomInt().let { value ->
                    Arguments.of(Filters.greaterThan(fieldName, value), "{\"$fieldName\":{\"\$gt\":$value}}")
                }
            },
            randomString().let { fieldName ->
                randomBoolean().let { value ->
                    Arguments.of(Filters.greaterThan(fieldName, value), "{\"$fieldName\":{\"\$gt\":$value}}")
                }
            },
            randomString().let { fieldName ->
                randomString().let { value ->
                    Arguments.of(
                        Filters.greaterThanEquals(fieldName, value),
                        "{\"$fieldName\":{\"\$gte\":\"$value\"}}",
                    )
                }
            },
            randomString().let { fieldName ->
                randomInt().let { value ->
                    Arguments.of(
                        Filters.greaterThanEquals(fieldName, value),
                        "{\"$fieldName\":{\"\$gte\":$value}}",
                    )
                }
            },
            randomString().let { fieldName ->
                randomBoolean().let { value ->
                    Arguments.of(
                        Filters.greaterThanEquals(fieldName, value),
                        "{\"$fieldName\":{\"\$gte\":$value}}",
                    )
                }
            },
            randomString().let { fieldName ->
                randomString().let { value ->
                    Arguments.of(Filters.lessThan(fieldName, value), "{\"$fieldName\":{\"\$lt\":\"$value\"}}")
                }
            },
            randomString().let { fieldName ->
                randomInt().let { value ->
                    Arguments.of(Filters.lessThan(fieldName, value), "{\"$fieldName\":{\"\$lt\":$value}}")
                }
            },
            randomString().let { fieldName ->
                randomBoolean().let { value ->
                    Arguments.of(Filters.lessThan(fieldName, value), "{\"$fieldName\":{\"\$lt\":$value}}")
                }
            },
            randomString().let { fieldName ->
                randomString().let { value ->
                    Arguments.of(
                        Filters.lessThanEquals(fieldName, value),
                        "{\"$fieldName\":{\"\$lte\":\"$value\"}}",
                    )
                }
            },
            randomString().let { fieldName ->
                randomInt().let { value ->
                    Arguments.of(Filters.lessThanEquals(fieldName, value), "{\"$fieldName\":{\"\$lte\":$value}}")
                }
            },
            randomString().let { fieldName ->
                randomBoolean().let { value ->
                    Arguments.of(Filters.lessThanEquals(fieldName, value), "{\"$fieldName\":{\"\$lte\":$value}}")
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomString() }.distinct().let { values ->
                    Arguments.of(
                        Filters.`in`(fieldName, values),
                        "{\"$fieldName\":{\"\$in\":[\"${values.joinToString(separator = "\",\"")}\"]}}",
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomInt() }.distinct().let { values ->
                    Arguments.of(
                        Filters.`in`(fieldName, values),
                        "{\"$fieldName\":{\"\$in\":[${values.joinToString(separator = ",")}]}}",
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomBoolean() }.distinct().let { values ->
                    Arguments.of(
                        Filters.`in`(fieldName, values),
                        "{\"$fieldName\":{\"\$in\":[${values.joinToString(separator = ",")}]}}",
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomString() }.distinct().let { values ->
                    Arguments.of(
                        Filters.`nin`(fieldName, values),
                        "{\"$fieldName\":{\"\$nin\":[\"${values.joinToString(separator = "\",\"")}\"]}}",
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomInt() }.distinct().let { values ->
                    Arguments.of(
                        Filters.`nin`(fieldName, values),
                        "{\"$fieldName\":{\"\$nin\":[${values.joinToString(separator = ",")}]}}",
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomBoolean() }.distinct().let { values ->
                    Arguments.of(
                        Filters.`nin`(fieldName, values),
                        "{\"$fieldName\":{\"\$nin\":[${values.joinToString(separator = ",")}]}}",
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomInt() }.distinct().let { values ->
                    Arguments.of(
                        Filters.and(
                            Filters.`in`(fieldName, values),
                            Filters.eq(fieldName, values.first()),
                        ),
                        "{\"\$and\":[{\"$fieldName\":{\"\$in\":[${values.joinToString(separator = ",")}]}}," +
                            "{\"$fieldName\":{\"\$eq\":${values.first()}}}]}",
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomInt() }.distinct().let { values ->
                    Arguments.of(
                        Filters.or(
                            Filters.`in`(fieldName, values),
                            Filters.eq(fieldName, values.first()),
                        ),
                        "{\"\$or\":[{\"$fieldName\":{\"\$in\":[${values.joinToString(separator = ",")}]}}," +
                            "{\"$fieldName\":{\"\$eq\":${values.first()}}}]}",
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomInt() }.distinct().let { values ->
                    Arguments.of(
                        Filters.nor(
                            Filters.`in`(fieldName, values),
                            Filters.eq(fieldName, values.first()),
                        ),
                        "{\"\$nor\":[{\"$fieldName\":{\"\$in\":[${values.joinToString(separator = ",")}]}}," +
                            "{\"$fieldName\":{\"\$eq\":${values.first()}}}]}",
                    )
                }
            },
            List(positiveRandomInt(20)) { randomString() }.distinct().let { memberIds ->
                Arguments.of(
                    Filters.distinct(memberIds),
                    "{\"distinct\":true,\"members\":[\"${memberIds.joinToString(separator = "\",\"")}\"]}",
                )
            },
        )

        @Suppress("LongMethod")
        @JvmStatic
        fun stringToObjectArguments() = listOf(
            Arguments.of("{}", Filters.neutral()),
            randomString().let { Arguments.of("{\"$it\":{\"\$exists\":true}}", Filters.exists(it)) },
            randomString().let { Arguments.of("{\"$it\":{\"\$exists\":false}}", Filters.notExists(it)) },
            randomString().let { fieldName ->
                randomString().let { value ->
                    Arguments.of(
                        "{\"$fieldName\":{\"\$autocomplete\":\"$value\"}}",
                        Filters.autocomplete(fieldName, value),
                    )
                }
            },
            randomString().let { fieldName ->
                randomString().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$eq\":\"$value\"}}", Filters.eq(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                randomInt().toDouble().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$eq\":$value}}", Filters.eq(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                randomBoolean().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$eq\":$value}}", Filters.eq(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                randomString().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$ne\":\"$value\"}}", Filters.ne(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                randomInt().toDouble().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$ne\":$value}}", Filters.ne(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                randomBoolean().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$ne\":$value}}", Filters.ne(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                randomString().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$contains\":\"$value\"}}", Filters.contains(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                randomInt().toDouble().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$contains\":$value}}", Filters.contains(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                randomBoolean().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$contains\":$value}}", Filters.contains(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                randomString().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$gt\":\"$value\"}}", Filters.greaterThan(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                randomInt().toDouble().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$gt\":$value}}", Filters.greaterThan(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                randomBoolean().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$gt\":$value}}", Filters.greaterThan(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                randomString().let { value ->
                    Arguments.of(
                        "{\"$fieldName\":{\"\$gte\":\"$value\"}}",
                        Filters.greaterThanEquals(fieldName, value),
                    )
                }
            },
            randomString().let { fieldName ->
                randomInt().toDouble().let { value ->
                    Arguments.of(
                        "{\"$fieldName\":{\"\$gte\":$value}}",
                        Filters.greaterThanEquals(fieldName, value),
                    )
                }
            },
            randomString().let { fieldName ->
                randomBoolean().let { value ->
                    Arguments.of(
                        "{\"$fieldName\":{\"\$gte\":$value}}",
                        Filters.greaterThanEquals(fieldName, value),
                    )
                }
            },
            randomString().let { fieldName ->
                randomString().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$lt\":\"$value\"}}", Filters.lessThan(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                randomInt().toDouble().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$lt\":$value}}", Filters.lessThan(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                randomBoolean().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$lt\":$value}}", Filters.lessThan(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                randomString().let { value ->
                    Arguments.of(
                        "{\"$fieldName\":{\"\$lte\":\"$value\"}}",
                        Filters.lessThanEquals(fieldName, value),
                    )
                }
            },
            randomString().let { fieldName ->
                randomInt().toDouble().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$lte\":$value}}", Filters.lessThanEquals(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                randomBoolean().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$lte\":$value}}", Filters.lessThanEquals(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomString() }.distinct().let { values ->
                    Arguments.of(
                        "{\"$fieldName\":{\"\$in\":[\"${values.joinToString(separator = "\",\"")}\"]}}",
                        Filters.`in`(fieldName, values),
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomInt().toDouble() }.distinct().let { values ->
                    Arguments.of(
                        "{\"$fieldName\":{\"\$in\":[${values.joinToString(separator = ",")}]}}",
                        Filters.`in`(fieldName, values),
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomBoolean() }.distinct().let { values ->
                    Arguments.of(
                        "{\"$fieldName\":{\"\$in\":[${values.joinToString(separator = ",")}]}}",
                        Filters.`in`(fieldName, values),
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomString() }.distinct().let { values ->
                    Arguments.of(
                        "{\"$fieldName\":{\"\$nin\":[\"${values.joinToString(separator = "\",\"")}\"]}}",
                        Filters.`nin`(fieldName, values),
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomInt().toDouble() }.distinct().let { values ->
                    Arguments.of(
                        "{\"$fieldName\":{\"\$nin\":[${values.joinToString(separator = ",")}]}}",
                        Filters.`nin`(fieldName, values),
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomBoolean() }.distinct().let { values ->
                    Arguments.of(
                        "{\"$fieldName\":{\"\$nin\":[${values.joinToString(separator = ",")}]}}",
                        Filters.`nin`(fieldName, values),
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomInt().toDouble() }.distinct().let { values ->
                    Arguments.of(
                        "{\"\$and\":[{\"$fieldName\":{\"\$in\":[${values.joinToString(separator = ",")}]}}," +
                            "{\"$fieldName\":{\"\$eq\":${values.first()}}}]}",
                        Filters.and(
                            Filters.`in`(fieldName, values),
                            Filters.eq(fieldName, values.first()),
                        ),
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomInt().toDouble() }.distinct().let { values ->
                    Arguments.of(
                        "{\"\$or\":[{\"$fieldName\":{\"\$in\":[${values.joinToString(separator = ",")}]}}," +
                            "{\"$fieldName\":{\"\$eq\":${values.first()}}}]}",
                        Filters.or(
                            Filters.`in`(fieldName, values),
                            Filters.eq(fieldName, values.first()),
                        ),
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomInt().toDouble() }.distinct().let { values ->
                    Arguments.of(
                        "{\"\$nor\":[{\"$fieldName\":{\"\$in\":[${values.joinToString(separator = ",")}]}}," +
                            "{\"$fieldName\":{\"\$eq\":${values.first()}}}]}",
                        Filters.nor(
                            Filters.`in`(fieldName, values),
                            Filters.eq(fieldName, values.first()),
                        ),
                    )
                }
            },
            List(positiveRandomInt(20)) { randomString() }.distinct().let { memberIds ->
                Arguments.of(
                    "{\"distinct\":true,\"members\":[\"${memberIds.joinToString(separator = "\",\"")}\"]}",
                    Filters.distinct(memberIds),
                )
            },
        )
    }
}
