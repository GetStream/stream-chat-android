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
 
package io.getstream.chat.android.offline.utils

import io.getstream.chat.android.client.utils.Result
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Assert.fail

internal infix fun <T, R : Result<T>> R.`should be equal to result`(expected: R) = apply {
    if (isError && expected.isError) {
        val thisError = this.error()
        val expectedError = expected.error()
        if (thisError.message != expectedError.message || thisError.cause != expectedError.cause) {
            fail("Expected $expected, actual $this")
        }
    } else {
        this.shouldBeEqualTo(expected)
    }
}
