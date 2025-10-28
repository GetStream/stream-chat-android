/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.utils.internal

import io.getstream.result.Error
import io.getstream.result.Result
import org.amshove.kluent.invoking
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should throw`
import org.amshove.kluent.`with message`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KClass

internal class ValidationKtTest {

    /** [argumentsValidateCidError] */
    @ParameterizedTest
    @MethodSource("argumentsValidateCidError")
    fun testValidateCidError(cid: String, expectedException: KClass<Exception>, expectedMessage: String) {
        invoking { validateCid(cid) } `should throw` expectedException `with message` expectedMessage
    }

    /** [argumentsValidateCid] */
    @ParameterizedTest
    @MethodSource("argumentsValidateCid")
    fun testValidateCid(cid: String, expectedCid: String) {
        validateCid(cid) `should be equal to` expectedCid
    }

    /** [argumentsValidCidResult] */
    @ParameterizedTest
    @MethodSource("argumentsValidCidResult")
    fun testValidateCidResult(cid: String, expectedResult: Result<String>) {
        validateCidWithResult(cid) `should be equal to` expectedResult
    }

    @Suppress("MaxLineLength")
    companion object {

        @JvmStatic
        fun argumentsValidCidResult() = validCids().map {
            Arguments.of(it, Result.Success(it))
        } +
            invalidCids().map {
                Arguments.of(
                    it.first,
                    Result.Failure(
                        Error.ThrowableError(
                            message = "Cid is invalid: ${it.first}",
                            cause = it.second,
                        ),
                    ),
                )
            }

        @JvmStatic
        fun argumentsValidateCid() = validCids().map {
            Arguments.of(it, it)
        }

        @JvmStatic
        fun argumentsValidateCidError() = invalidCids().map {
            Arguments.of(
                it.first,
                it.second::class,
                it.second.message,
            )
        }

        private fun invalidCids(): List<Pair<String, Exception>> = listOf(
            "" to IllegalArgumentException("cid can not be empty"),
            "   " to IllegalArgumentException("cid can not be blank"),
            "messaging 123" to IllegalArgumentException("cid needs to be in the format channelType:channelId. For example, messaging:123"),
            "messaging123" to IllegalArgumentException("cid needs to be in the format channelType:channelId. For example, messaging:123"),
            "messaging::123" to IllegalArgumentException("cid needs to be in the format channelType:channelId. For example, messaging:123"),
            "messaging:" to IllegalArgumentException("cid needs to be in the format channelType:channelId. For example, messaging:123"),
            ":123" to IllegalArgumentException("cid needs to be in the format channelType:channelId. For example, messaging:123"),
            "mess aging:123" to IllegalArgumentException("cid needs to be in the format channelType:channelId. For example, messaging:123"),
            ":" to IllegalArgumentException("cid needs to be in the format channelType:channelId. For example, messaging:123"),
        )

        private fun validCids() = listOf(
            "messaging:123",
            "a:e",
            "messaging:!members-oNJ1lQqt2b9SKG6raDWRTn4wWLakkFkwvqlUn-EsatU",
            "!members-oNJ1lQqt2b9SKG6raDWRTn4wWLakkFkwvqlUn-EsatU:!members-oNJ1lQqt2b9SKG6raDWRTn4wWLakkFkwvqlUn-EsatU",
        )
    }
}
