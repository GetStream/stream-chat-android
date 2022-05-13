/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.offline.utils.internal

import org.amshove.kluent.invoking
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should throw`
import org.amshove.kluent.`with message`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass

internal class ValidationKtTest {

    /** [argumentsValidateCidBoolean] */
    @ParameterizedTest
    @MethodSource("argumentsValidateCidBoolean")
    fun testValidateCidBoolean(cid: String, expectedValidation: Boolean) {
        validateCidBoolean(cid) `should be equal to` expectedValidation
    }

    /** [argumentsValidateCidError] */
    @ParameterizedTest
    @MethodSource("argumentsValidateCidError")
    fun testValidateCidError(cid: String, expectedException: KClass<Exception>, expectedMessage: String) {
        invoking { validateCid(cid) } `should throw` expectedException `with message` expectedMessage
    }

    /** [argumentsValidateCid] */
    @ParameterizedTest
    @MethodSource("argumentsValidateCid")
    fun testValidateCid(cid: String) {
        validateCid(cid) `should be equal to` cid
    }

    @Suppress("MaxLineLength")
    companion object {

        @JvmStatic
        fun argumentsValidateCidBoolean() = listOf(
            Arguments.of("messaging:123", true),
            Arguments.of("a:e", true),
            Arguments.of("messaging:!members-oNJ1lQqt2b9SKG6raDWRTn4wWLakkFkwvqlUn-EsatU", true),
            Arguments.of("!members-oNJ1lQqt2b9SKG6raDWRTn4wWLakkFkwvqlUn-EsatU:!members-oNJ1lQqt2b9SKG6raDWRTn4wWLakkFkwvqlUn-EsatU", true),
            Arguments.of("", false),
            Arguments.of("   ", false),
            Arguments.of("messaging 123", false),
            Arguments.of("messaging123", false),
            Arguments.of("messaging::123", false),
            Arguments.of("messaging:", false),
            Arguments.of(":123", false),
            Arguments.of("mess aging:123", false),
            Arguments.of(":", false),
        )

        @JvmStatic
        fun argumentsValidateCid() = listOf(
            Arguments.of("messaging:123"),
            Arguments.of("a:e"),
            Arguments.of("messaging:!members-oNJ1lQqt2b9SKG6raDWRTn4wWLakkFkwvqlUn-EsatU"),
            Arguments.of("!members-oNJ1lQqt2b9SKG6raDWRTn4wWLakkFkwvqlUn-EsatU:!members-oNJ1lQqt2b9SKG6raDWRTn4wWLakkFkwvqlUn-EsatU"),
        )

        @JvmStatic
        fun argumentsValidateCidError() = listOf(
            Arguments.of(
                "",
                IllegalArgumentException::class,
                "cid can not be empty"
            ),
            Arguments.of(
                "   ",
                IllegalArgumentException::class,
                "cid can not be blank"
            ),
            Arguments.of(
                "messaging 123",
                IllegalArgumentException::class,
                "cid needs to be in the format channelType:channelId. For example, messaging:123"
            ),
            Arguments.of(
                "messaging123",
                IllegalArgumentException::class,
                "cid needs to be in the format channelType:channelId. For example, messaging:123"
            ),
            Arguments.of(
                "messaging::123",
                IllegalArgumentException::class,
                "cid needs to be in the format channelType:channelId. For example, messaging:123"
            ),
            Arguments.of(
                "messaging:",
                IllegalArgumentException::class,
                "cid needs to be in the format channelType:channelId. For example, messaging:123"
            ),
            Arguments.of(
                ":123",
                IllegalArgumentException::class,
                "cid needs to be in the format channelType:channelId. For example, messaging:123"
            ),
            Arguments.of(
                "mess aging:123",
                IllegalArgumentException::class,
                "cid needs to be in the format channelType:channelId. For example, messaging:123"
            ),
            Arguments.of(
                ":",
                IllegalArgumentException::class,
                "cid needs to be in the format channelType:channelId. For example, messaging:123"
            ),
        )
    }
}
