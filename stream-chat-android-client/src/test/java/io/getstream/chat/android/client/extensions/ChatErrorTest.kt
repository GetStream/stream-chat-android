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

package io.getstream.chat.android.client.extensions

import io.getstream.chat.android.client.errors.isPermanent
import io.getstream.result.Error
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.junit.Test
import java.net.ConnectException
import java.net.UnknownHostException

internal class ChatErrorTest {

    @Test
    fun `error for messages with the same ID should be permanent`() {
        val error = Error.NetworkError(
            serverErrorCode = 4,
            message = "a message with ID the same id already exists",
            statusCode = 400,
        )
        error.isPermanent().shouldBeTrue()
    }

    @Test
    fun `rateLimit error should be temporary`() {
        val error = Error.NetworkError(
            serverErrorCode = 9,
            message = "",
            statusCode = 429,
        )
        error.isPermanent().shouldBeFalse()
    }

    @Test
    fun `request timeout should be a temporary error`() {
        val error = Error.NetworkError(
            serverErrorCode = 23,
            message = "",
            statusCode = 408,
        )
        error.isPermanent().shouldBeFalse()
    }

    @Test
    fun `broken api should be a temporary error`() {
        val error = Error.NetworkError(
            serverErrorCode = 0,
            message = "",
            statusCode = 500,
        )
        error.isPermanent().shouldBeFalse()
    }

    @Test
    fun `cool down period error should be permanent`() {
        val error = Error.NetworkError(
            serverErrorCode = 60,
            message = "",
            statusCode = 403,
        )
        error.isPermanent().shouldBeTrue()
    }

    @Test
    fun `UnknownHost as cause should be a temporary error`() {
        val error = Error.NetworkError(
            serverErrorCode = 0,
            message = "",
            statusCode = 500,
            cause = UnknownHostException(),
        )
        error.isPermanent().shouldBeFalse()
    }

    @Test
    fun `ConnectException as cause should be a temporary error`() {
        val error = Error.NetworkError(
            serverErrorCode = 0,
            message = "",
            statusCode = 500,
            cause = ConnectException(),
        )
        error.isPermanent().shouldBeFalse()
    }
}
