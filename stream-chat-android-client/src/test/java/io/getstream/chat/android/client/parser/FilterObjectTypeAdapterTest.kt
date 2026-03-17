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

package io.getstream.chat.android.client.parser

import io.getstream.chat.android.client.parser2.ParserFactory
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.NeutralFilterObject
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomString
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class FilterObjectTypeAdapterTest {

    private val filterObjectAdapter = ParserFactory.createMoshiChatParser()

    /** [writeArguments] */
    @ParameterizedTest
    @MethodSource("writeArguments")
    fun writeTest(filterObject: FilterObject, expectedJson: String) {
        val result = filterObjectAdapter.toJson(filterObject.toMap())
        result shouldBeEqualTo expectedJson
    }

    companion object {

        @JvmStatic
        @Suppress("LongMethod", "ComplexMethod")
        fun writeArguments() = listOf(
            Arguments.of(NeutralFilterObject, "{}"),
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
                    Arguments.of(Filters.eq(fieldName, value), "{\"$fieldName\":\"$value\"}")
                }
            },
            randomString().let { fieldName ->
                randomInt().let { value ->
                    Arguments.of(Filters.eq(fieldName, value), "{\"$fieldName\":$value}")
                }
            },
            randomString().let { fieldName ->
                randomBoolean().let { value ->
                    Arguments.of(Filters.eq(fieldName, value), "{\"$fieldName\":$value}")
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
                List(positiveRandomInt(20)) { randomString() }.let { values ->
                    Arguments.of(
                        Filters.`in`(fieldName, values),
                        "{\"$fieldName\":{\"\$in\":[\"${values.toSet().joinToString(separator = "\",\"")}\"]}}",
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomInt() }.let { values ->
                    Arguments.of(
                        Filters.`in`(fieldName, values),
                        "{\"$fieldName\":{\"\$in\":[${values.toSet().joinToString(separator = ",")}]}}",
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomBoolean() }.let { values ->
                    Arguments.of(
                        Filters.`in`(fieldName, values),
                        "{\"$fieldName\":{\"\$in\":[${values.toSet().joinToString(separator = ",")}]}}",
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomString() }.let { values ->
                    Arguments.of(
                        Filters.nin(fieldName, values),
                        "{\"$fieldName\":{\"\$nin\":[\"${values.toSet().joinToString(separator = "\",\"")}\"]}}",
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomInt() }.let { values ->
                    Arguments.of(
                        Filters.nin(fieldName, values),
                        "{\"$fieldName\":{\"\$nin\":[${values.toSet().joinToString(separator = ",")}]}}",
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomBoolean() }.let { values ->
                    Arguments.of(
                        Filters.nin(fieldName, values),
                        "{\"$fieldName\":{\"\$nin\":[${values.toSet().joinToString(separator = ",")}]}}",
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomInt() }.let { values ->
                    Arguments.of(
                        Filters.and(
                            Filters.`in`(fieldName, values),
                            Filters.eq(fieldName, values.first()),
                        ),
                        "{\"\$and\":[{\"$fieldName\":{\"\$in\":[${values.toSet().joinToString(separator = ",")}]}}" +
                            ",{\"$fieldName\":${values.first()}}]}",
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomInt() }.let { values ->
                    Arguments.of(
                        Filters.or(
                            Filters.`in`(fieldName, values),
                            Filters.eq(fieldName, values.first()),
                        ),
                        "{\"\$or\":[{\"$fieldName\":{\"\$in\":[${values.toSet().joinToString(separator = ",")}]}}" +
                            ",{\"$fieldName\":${values.first()}}]}",
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomInt() }.let { values ->
                    Arguments.of(
                        Filters.nor(
                            Filters.`in`(fieldName, values),
                            Filters.eq(fieldName, values.first()),
                        ),
                        "{\"\$nor\":[{\"$fieldName\":{\"\$in\":[${values.toSet().joinToString(separator = ",")}]}}" +
                            ",{\"$fieldName\":${values.first()}}]}",
                    )
                }
            },
            List(positiveRandomInt(20)) { randomString() }.let { memberIds ->
                Arguments.of(
                    Filters.distinct(memberIds),
                    "{\"distinct\":true,\"members\":[\"${memberIds.toSet().joinToString(separator = "\",\"")}\"]}",
                )
            },
        )
    }
}
