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

package io.getstream.chat.android.client.api2.model.response

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.api2.endpoint.ThreadsApi
import io.getstream.chat.android.client.api2.model.dto.DownstreamThreadDto

/**
 * Response for [ThreadsApi.queryThreads]
 *
 * @param threads: The list of threads.
 * @param duration: The duration of the request.
 * @param prev: The identifier for the previous page of threads.
 * @param next: The identifier for the next page of threads.
 */
@JsonClass(generateAdapter = true)
internal data class QueryThreadsResponse(
    val threads: List<DownstreamThreadDto>,
    val duration: String,
    val prev: String?,
    val next: String?,
)
