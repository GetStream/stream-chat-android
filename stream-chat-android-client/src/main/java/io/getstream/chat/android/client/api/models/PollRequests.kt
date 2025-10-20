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

package io.getstream.chat.android.client.api.models

/**
 * Model holding request data for creating a poll option.
 *
 * @param text The text of the poll option.
 * @param extraData A map of additional key-value pairs to include in the creation request.
 */
public data class CreatePollOptionRequest(
    val text: String,
    val extraData: Map<String, Any> = emptyMap(),
)

/**
 * Model holding request data for updating a poll option.
 *
 * @param id The unique identifier of the poll option to be updated.
 * @param text The new text for the poll option.
 * @param extraData A map of additional key-value pairs to include in the update request.
 */
public data class UpdatePollOptionRequest(
    val id: String,
    val text: String,
    val extraData: Map<String, Any> = emptyMap(),
)
