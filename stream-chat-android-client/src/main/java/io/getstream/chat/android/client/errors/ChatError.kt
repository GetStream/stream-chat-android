/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

/**
 * The error response from the chat server.
 *
 * @property code The error code.
 * @property message The error message.
 * @property statusCode The status code.
 * @property exceptionFields The exception fields.
 * @property moreInfo More info about the error.
 * @property details The error details.
 * @property duration The duration of the error.
 */
public data class ChatError(
    val code: Int = -1,
    var message: String = "",
    var statusCode: Int = -1,
    val exceptionFields: Map<String, String> = mapOf(),
    var moreInfo: String = "",
    val details: List<ChatErrorDetail> = emptyList(),
    var duration: String = "",
)

/**
 * The error detail.
 *
 * @property code The error code.
 * @property messages The error messages.
 */
public data class ChatErrorDetail(
    public val code: Int,
    public val messages: List<String>,
)
