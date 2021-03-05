package io.getstream.chat.android.client.parser

import com.google.gson.Gson
import com.google.gson.stream.JsonWriter
import io.getstream.chat.android.client.api.models.AndFilterObject
import io.getstream.chat.android.client.api.models.AutocompleteFilterObject
import io.getstream.chat.android.client.api.models.ContainsFilterObject
import io.getstream.chat.android.client.api.models.DistinctFilterObject
import io.getstream.chat.android.client.api.models.EqualsFilterObject
import io.getstream.chat.android.client.api.models.ExistsFilterObject
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.GreaterThanFilterObject
import io.getstream.chat.android.client.api.models.GreaterThanOrEqualsFilterObject
import io.getstream.chat.android.client.api.models.InFilterObject
import io.getstream.chat.android.client.api.models.LessThanFilterObject
import io.getstream.chat.android.client.api.models.LessThanOrEqualsFilterObject
import io.getstream.chat.android.client.api.models.NeutralFilterObject
import io.getstream.chat.android.client.api.models.NonExistsFilterObject
import io.getstream.chat.android.client.api.models.NorFilterObject
import io.getstream.chat.android.client.api.models.NotEqualsFilterObject
import io.getstream.chat.android.client.api.models.NotInFilterObject
import io.getstream.chat.android.client.api.models.OrFilterObject
import io.getstream.chat.android.test.positiveRandomInt
import io.getstream.chat.android.test.randomBoolean
import io.getstream.chat.android.test.randomInt
import io.getstream.chat.android.test.randomString
import org.junit.Assert
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.StringWriter

internal class FilterObjectTypeAdapterTest {

    private val filterObjectAdapter = FilterObjectTypeAdapter(Gson())

    /** [writeArguments] */
    @ParameterizedTest
    @MethodSource("writeArguments")
    fun writeTest(filterObject: FilterObject, expectedJson: String) {
        val stringWriter = StringWriter()

        filterObjectAdapter.write(JsonWriter(stringWriter), filterObject)

        Assert.assertEquals(expectedJson, stringWriter.toString())
    }

    companion object {

        @JvmStatic
        fun writeArguments() = listOf(
            Arguments.of(NeutralFilterObject, "{}"),
            randomString().let { Arguments.of(ExistsFilterObject(it), "{\"$it\":{\"\$exists\":true}}") },
            randomString().let { Arguments.of(NonExistsFilterObject(it), "{\"$it\":{\"\$exists\":false}}") },
            randomString().let { fieldName ->
                randomString().let { value ->
                    Arguments.of(
                        AutocompleteFilterObject(fieldName, value),
                        "{\"$fieldName\":{\"\$autocomplete\":\"$value\"}}"
                    )
                }
            },
            randomString().let { fieldName ->
                randomString().let { value ->
                    Arguments.of(EqualsFilterObject(fieldName, value), "{\"$fieldName\":\"$value\"}")
                }
            },
            randomString().let { fieldName ->
                randomInt().let { value ->
                    Arguments.of(EqualsFilterObject(fieldName, value), "{\"$fieldName\":$value}")
                }
            },
            randomString().let { fieldName ->
                randomBoolean().let { value ->
                    Arguments.of(EqualsFilterObject(fieldName, value), "{\"$fieldName\":$value}")
                }
            },
            randomString().let { fieldName ->
                randomString().let { value ->
                    Arguments.of(NotEqualsFilterObject(fieldName, value), "{\"$fieldName\":{\"\$ne\":\"$value\"}}")
                }
            },
            randomString().let { fieldName ->
                randomInt().let { value ->
                    Arguments.of(NotEqualsFilterObject(fieldName, value), "{\"$fieldName\":{\"\$ne\":$value}}")
                }
            },
            randomString().let { fieldName ->
                randomBoolean().let { value ->
                    Arguments.of(NotEqualsFilterObject(fieldName, value), "{\"$fieldName\":{\"\$ne\":$value}}")
                }
            },
            randomString().let { fieldName ->
                randomString().let { value ->
                    Arguments.of(ContainsFilterObject(fieldName, value), "{\"$fieldName\":{\"\$contains\":\"$value\"}}")
                }
            },
            randomString().let { fieldName ->
                randomInt().let { value ->
                    Arguments.of(ContainsFilterObject(fieldName, value), "{\"$fieldName\":{\"\$contains\":$value}}")
                }
            },
            randomString().let { fieldName ->
                randomBoolean().let { value ->
                    Arguments.of(ContainsFilterObject(fieldName, value), "{\"$fieldName\":{\"\$contains\":$value}}")
                }
            },
            randomString().let { fieldName ->
                randomString().let { value ->
                    Arguments.of(GreaterThanFilterObject(fieldName, value), "{\"$fieldName\":{\"\$gt\":\"$value\"}}")
                }
            },
            randomString().let { fieldName ->
                randomInt().let { value ->
                    Arguments.of(GreaterThanFilterObject(fieldName, value), "{\"$fieldName\":{\"\$gt\":$value}}")
                }
            },
            randomString().let { fieldName ->
                randomBoolean().let { value ->
                    Arguments.of(GreaterThanFilterObject(fieldName, value), "{\"$fieldName\":{\"\$gt\":$value}}")
                }
            },
            randomString().let { fieldName ->
                randomString().let { value ->
                    Arguments.of(
                        GreaterThanOrEqualsFilterObject(fieldName, value),
                        "{\"$fieldName\":{\"\$gte\":\"$value\"}}"
                    )
                }
            },
            randomString().let { fieldName ->
                randomInt().let { value ->
                    Arguments.of(
                        GreaterThanOrEqualsFilterObject(fieldName, value),
                        "{\"$fieldName\":{\"\$gte\":$value}}"
                    )
                }
            },
            randomString().let { fieldName ->
                randomBoolean().let { value ->
                    Arguments.of(
                        GreaterThanOrEqualsFilterObject(fieldName, value),
                        "{\"$fieldName\":{\"\$gte\":$value}}"
                    )
                }
            },
            randomString().let { fieldName ->
                randomString().let { value ->
                    Arguments.of(LessThanFilterObject(fieldName, value), "{\"$fieldName\":{\"\$lt\":\"$value\"}}")
                }
            },
            randomString().let { fieldName ->
                randomInt().let { value ->
                    Arguments.of(LessThanFilterObject(fieldName, value), "{\"$fieldName\":{\"\$lt\":$value}}")
                }
            },
            randomString().let { fieldName ->
                randomBoolean().let { value ->
                    Arguments.of(LessThanFilterObject(fieldName, value), "{\"$fieldName\":{\"\$lt\":$value}}")
                }
            },
            randomString().let { fieldName ->
                randomString().let { value ->
                    Arguments.of(
                        LessThanOrEqualsFilterObject(fieldName, value),
                        "{\"$fieldName\":{\"\$lte\":\"$value\"}}"
                    )
                }
            },
            randomString().let { fieldName ->
                randomInt().let { value ->
                    Arguments.of(LessThanOrEqualsFilterObject(fieldName, value), "{\"$fieldName\":{\"\$lte\":$value}}")
                }
            },
            randomString().let { fieldName ->
                randomBoolean().let { value ->
                    Arguments.of(LessThanOrEqualsFilterObject(fieldName, value), "{\"$fieldName\":{\"\$lte\":$value}}")
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomString() }.toSet().let { values ->
                    Arguments.of(
                        InFilterObject(fieldName, values),
                        "{\"$fieldName\":{\"\$in\":[\"${values.joinToString(separator = "\",\"")}\"]}}"
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomInt() }.toSet().let { values ->
                    Arguments.of(
                        InFilterObject(fieldName, values),
                        "{\"$fieldName\":{\"\$in\":[${values.joinToString(separator = ",")}]}}"
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomBoolean() }.toSet().let { values ->
                    Arguments.of(
                        InFilterObject(fieldName, values),
                        "{\"$fieldName\":{\"\$in\":[${values.joinToString(separator = ",")}]}}"
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomString() }.toSet().let { values ->
                    Arguments.of(
                        NotInFilterObject(fieldName, values),
                        "{\"$fieldName\":{\"\$nin\":[\"${values.joinToString(separator = "\",\"")}\"]}}"
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomInt() }.toSet().let { values ->
                    Arguments.of(
                        NotInFilterObject(fieldName, values),
                        "{\"$fieldName\":{\"\$nin\":[${values.joinToString(separator = ",")}]}}"
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomBoolean() }.toSet().let { values ->
                    Arguments.of(
                        NotInFilterObject(fieldName, values),
                        "{\"$fieldName\":{\"\$nin\":[${values.joinToString(separator = ",")}]}}"
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomInt() }.toSet().let { values ->
                    Arguments.of(
                        AndFilterObject(
                            setOf(
                                InFilterObject(fieldName, values),
                                EqualsFilterObject(fieldName, values.first())
                            )
                        ),
                        "{\"\$and\":[{\"$fieldName\":{\"\$in\":[${values.joinToString(separator = ",")}]}},{\"$fieldName\":${values.first()}}]}"
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomInt() }.toSet().let { values ->
                    Arguments.of(
                        OrFilterObject(
                            setOf(
                                InFilterObject(fieldName, values),
                                EqualsFilterObject(fieldName, values.first())
                            )
                        ),
                        "{\"\$or\":[{\"$fieldName\":{\"\$in\":[${values.joinToString(separator = ",")}]}},{\"$fieldName\":${values.first()}}]}"
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomInt() }.toSet().let { values ->
                    Arguments.of(
                        NorFilterObject(
                            setOf(
                                InFilterObject(fieldName, values),
                                EqualsFilterObject(fieldName, values.first())
                            )
                        ),
                        "{\"\$nor\":[{\"$fieldName\":{\"\$in\":[${values.joinToString(separator = ",")}]}},{\"$fieldName\":${values.first()}}]}"
                    )
                }
            },
            List(positiveRandomInt(20)) { randomString() }.toSet().let { memberIds ->
                Arguments.of(
                    DistinctFilterObject(memberIds),
                    "{\"distinct\":true,\"members\":[\"${memberIds.joinToString(separator = "\",\"")}\"]}"
                )
            }
        )
    }
}
