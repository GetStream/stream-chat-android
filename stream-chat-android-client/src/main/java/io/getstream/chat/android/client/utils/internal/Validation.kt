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

package io.getstream.chat.android.client.utils.internal

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.result.Error
import io.getstream.result.Result
import java.util.regex.Pattern

private val cidPattern = Pattern.compile("^([a-zA-z0-9]|!|-)+:([a-zA-z0-9]|!|-)+$")

/**
 * Validates a cid. Verifies it's not empty and in the format messaging:123.
 *
 * @param cid The full channel id. ie messaging:123.
 *
 * @throws IllegalArgumentException If CID is invalid.
 */
@Throws(IllegalArgumentException::class)
@InternalStreamChatApi
public fun validateCid(cid: String): String = cid.apply {
    require(cid.isNotEmpty()) { "cid can not be empty" }
    require(cid.isNotBlank()) { "cid can not be blank" }
    require(cidPattern.matcher(cid).matches()) {
        "cid needs to be in the format channelType:channelId. For example, messaging:123"
    }
}

/**
 * Safely validates a cid and returns a result.
 *
 * @param cid The full channel id. ie messaging:123.
 *
 * @return Successful [Result] if the cid is valid.
 */
@InternalStreamChatApi
public fun validateCidWithResult(cid: String): Result<String> = try {
    Result.Success(validateCid(cid))
} catch (exception: IllegalArgumentException) {
    Result.Failure(Error.ThrowableError(message = "Cid is invalid: $cid", cause = exception))
}
