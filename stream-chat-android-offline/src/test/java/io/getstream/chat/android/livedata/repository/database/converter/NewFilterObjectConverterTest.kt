package io.getstream.chat.android.livedata.repository.database.converter

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

internal class NewFilterObjectConverterTest {

    private val filterObjectConverter = NewFilterObjectConverter()

    /** [objectToStringArguments] */
    @ParameterizedTest
    @MethodSource("objectToStringArguments")
    fun objectToStringTest(filterObject: FilterObject, expectedJson: String) {
        val result = filterObjectConverter.objectToString(filterObject)

        Assert.assertEquals(expectedJson, result)
    }

    /** [stringToObjectArguments] */
    @ParameterizedTest
    @MethodSource("stringToObjectArguments")
    fun stringToObjectTest(json: String, expectedFilterObject: FilterObject) {
        val result = filterObjectConverter.stringToObject(json)

        Assert.assertEquals(expectedFilterObject, result)
    }

    companion object {

        @JvmStatic
        fun objectToStringArguments() = listOf(
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
                    Arguments.of(EqualsFilterObject(fieldName, value), "{\"$fieldName\":{\"\$eq\":\"$value\"}}")
                }
            },
            randomString().let { fieldName ->
                randomInt().let { value ->
                    Arguments.of(EqualsFilterObject(fieldName, value), "{\"$fieldName\":{\"\$eq\":$value}}")
                }
            },
            randomString().let { fieldName ->
                randomBoolean().let { value ->
                    Arguments.of(EqualsFilterObject(fieldName, value), "{\"$fieldName\":{\"\$eq\":$value}}")
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
                        "{\"\$and\":[{\"$fieldName\":{\"\$in\":[${values.joinToString(separator = ",")}]}},{\"$fieldName\":{\"\$eq\":${values.first()}}}]}"
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
                        "{\"\$or\":[{\"$fieldName\":{\"\$in\":[${values.joinToString(separator = ",")}]}},{\"$fieldName\":{\"\$eq\":${values.first()}}}]}"
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
                        "{\"\$nor\":[{\"$fieldName\":{\"\$in\":[${values.joinToString(separator = ",")}]}},{\"$fieldName\":{\"\$eq\":${values.first()}}}]}"
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

        @JvmStatic
        fun stringToObjectArguments() = listOf(
            Arguments.of("{}", NeutralFilterObject),
            randomString().let { Arguments.of("{\"$it\":{\"\$exists\":true}}", ExistsFilterObject(it)) },
            randomString().let { Arguments.of("{\"$it\":{\"\$exists\":false}}", NonExistsFilterObject(it)) },
            randomString().let { fieldName ->
                randomString().let { value ->
                    Arguments.of(
                        "{\"$fieldName\":{\"\$autocomplete\":\"$value\"}}",
                        AutocompleteFilterObject(fieldName, value)
                    )
                }
            },
            randomString().let { fieldName ->
                randomString().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$eq\":\"$value\"}}", EqualsFilterObject(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                randomInt().toDouble().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$eq\":$value}}", EqualsFilterObject(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                randomBoolean().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$eq\":$value}}", EqualsFilterObject(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                randomString().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$ne\":\"$value\"}}", NotEqualsFilterObject(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                randomInt().toDouble().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$ne\":$value}}", NotEqualsFilterObject(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                randomBoolean().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$ne\":$value}}", NotEqualsFilterObject(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                randomString().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$contains\":\"$value\"}}", ContainsFilterObject(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                randomInt().toDouble().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$contains\":$value}}", ContainsFilterObject(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                randomBoolean().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$contains\":$value}}", ContainsFilterObject(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                randomString().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$gt\":\"$value\"}}", GreaterThanFilterObject(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                randomInt().toDouble().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$gt\":$value}}", GreaterThanFilterObject(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                randomBoolean().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$gt\":$value}}", GreaterThanFilterObject(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                randomString().let { value ->
                    Arguments.of(
                        "{\"$fieldName\":{\"\$gte\":\"$value\"}}",
                        GreaterThanOrEqualsFilterObject(fieldName, value)
                    )
                }
            },
            randomString().let { fieldName ->
                randomInt().toDouble().let { value ->
                    Arguments.of(
                        "{\"$fieldName\":{\"\$gte\":$value}}",
                        GreaterThanOrEqualsFilterObject(fieldName, value)
                    )
                }
            },
            randomString().let { fieldName ->
                randomBoolean().let { value ->
                    Arguments.of(
                        "{\"$fieldName\":{\"\$gte\":$value}}",
                        GreaterThanOrEqualsFilterObject(fieldName, value)
                    )
                }
            },
            randomString().let { fieldName ->
                randomString().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$lt\":\"$value\"}}", LessThanFilterObject(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                randomInt().toDouble().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$lt\":$value}}", LessThanFilterObject(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                randomBoolean().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$lt\":$value}}", LessThanFilterObject(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                randomString().let { value ->
                    Arguments.of(
                        "{\"$fieldName\":{\"\$lte\":\"$value\"}}",
                        LessThanOrEqualsFilterObject(fieldName, value)
                    )
                }
            },
            randomString().let { fieldName ->
                randomInt().toDouble().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$lte\":$value}}", LessThanOrEqualsFilterObject(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                randomBoolean().let { value ->
                    Arguments.of("{\"$fieldName\":{\"\$lte\":$value}}", LessThanOrEqualsFilterObject(fieldName, value))
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomString() }.toSet().let { values ->
                    Arguments.of(
                        "{\"$fieldName\":{\"\$in\":[\"${values.joinToString(separator = "\",\"")}\"]}}",
                        InFilterObject(fieldName, values)
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomInt().toDouble() }.toSet().let { values ->
                    Arguments.of(
                        "{\"$fieldName\":{\"\$in\":[${values.joinToString(separator = ",")}]}}",
                        InFilterObject(fieldName, values)
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomBoolean() }.toSet().let { values ->
                    Arguments.of(
                        "{\"$fieldName\":{\"\$in\":[${values.joinToString(separator = ",")}]}}",
                        InFilterObject(fieldName, values)
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomString() }.toSet().let { values ->
                    Arguments.of(
                        "{\"$fieldName\":{\"\$nin\":[\"${values.joinToString(separator = "\",\"")}\"]}}",
                        NotInFilterObject(fieldName, values)
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomInt().toDouble() }.toSet().let { values ->
                    Arguments.of(
                        "{\"$fieldName\":{\"\$nin\":[${values.joinToString(separator = ",")}]}}",
                        NotInFilterObject(fieldName, values)
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomBoolean() }.toSet().let { values ->
                    Arguments.of(
                        "{\"$fieldName\":{\"\$nin\":[${values.joinToString(separator = ",")}]}}",
                        NotInFilterObject(fieldName, values)
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomInt().toDouble() }.toSet().let { values ->
                    Arguments.of(
                        "{\"\$and\":[{\"$fieldName\":{\"\$in\":[${values.joinToString(separator = ",")}]}},{\"$fieldName\":{\"\$eq\":${values.first()}}}]}",
                        AndFilterObject(
                            setOf(
                                InFilterObject(fieldName, values),
                                EqualsFilterObject(fieldName, values.first())
                            )
                        )
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomInt().toDouble() }.toSet().let { values ->
                    Arguments.of(
                        "{\"\$or\":[{\"$fieldName\":{\"\$in\":[${values.joinToString(separator = ",")}]}},{\"$fieldName\":{\"\$eq\":${values.first()}}}]}",
                        OrFilterObject(
                            setOf(
                                InFilterObject(fieldName, values),
                                EqualsFilterObject(fieldName, values.first())
                            )
                        )
                    )
                }
            },
            randomString().let { fieldName ->
                List(positiveRandomInt(20)) { randomInt().toDouble() }.toSet().let { values ->
                    Arguments.of(
                        "{\"\$nor\":[{\"$fieldName\":{\"\$in\":[${values.joinToString(separator = ",")}]}},{\"$fieldName\":{\"\$eq\":${values.first()}}}]}",
                        NorFilterObject(
                            setOf(
                                InFilterObject(fieldName, values),
                                EqualsFilterObject(fieldName, values.first())
                            )
                        )
                    )
                }
            },
            List(positiveRandomInt(20)) { randomString() }.toSet().let { memberIds ->
                Arguments.of(
                    "{\"distinct\":true,\"members\":[\"${memberIds.joinToString(separator = "\",\"")}\"]}",
                    DistinctFilterObject(memberIds)
                )
            }
        )
    }
}
