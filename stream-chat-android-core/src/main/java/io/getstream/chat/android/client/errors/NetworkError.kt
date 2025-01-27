/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.result.Error

private const val HTTP_BAD_REQUEST = 400

/**
 * Checks if the [Error.NetworkError] was caused by a bad request (HTTP 400).
 */
@InternalStreamChatApi
public fun Error.NetworkError.isStatusBadRequest(): Boolean {
    return statusCode == HTTP_BAD_REQUEST
}

/**
 * Checks if the [Error.NetworkError] was caused by a server error code: [VALIDATION_ERROR_ERROR_CODE].
 */
@InternalStreamChatApi
public fun Error.NetworkError.isValidationError(): Boolean {
    return serverErrorCode == ChatErrorCode.VALIDATION_ERROR.code
}
