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

package io.getstream.chat.android.client.errors

import io.getstream.result.Error
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class NetworkErrorTest {

    @Test
    fun testIsStatusBadRequest() {
        val error = Error.NetworkError("Error", ChatErrorCode.NETWORK_FAILED.code, 400)
        Assertions.assertTrue(error.isStatusBadRequest())
    }

    @Test
    fun testIsValidationError() {
        val error = Error.NetworkError("Error", ChatErrorCode.VALIDATION_ERROR.code, 400)
        Assertions.assertTrue(error.isValidationError())
    }
}
