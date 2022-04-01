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
 
package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.errors.ChatNetworkError
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldBeTrue

internal fun <T : Any> verifyError(result: Result<T>, statusCode: Int) {
    result.isSuccess.shouldBeFalse()
    result.error().shouldBeInstanceOf<ChatNetworkError>()

    val error = result.error() as ChatNetworkError
    error.statusCode shouldBeEqualTo statusCode
}

internal fun <T : Any> verifySuccess(result: Result<T>, equalsTo: T) {
    result.isSuccess.shouldBeTrue()
    result.data() shouldBeEqualTo equalsTo
}
