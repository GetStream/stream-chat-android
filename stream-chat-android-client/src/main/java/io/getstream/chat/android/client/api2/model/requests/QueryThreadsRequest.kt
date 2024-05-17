/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.api2.endpoint.ThreadsApi

/**
 * Used for querying threads.
 * @see [ThreadsApi.getThreads]
 *
 * @param reply_limit The number of latest replies to fetch per thread. Defaults to 2.
 * @param participant_limit The number of thread participants to request per thread. Defaults to 100.
 * @param limit The number of threads to return. Defaults to 10.
 * @param watch If true, all the channels corresponding to threads returned in response will be watched.
 * Defaults to true.
 * @param member_limit The number of members to request per thread. Defaults to 100.
 */
@JsonClass(generateAdapter = true)
internal data class QueryThreadsRequest(
    val reply_limit: Int = 2,
    val participant_limit: Int = 100,
    val limit: Int = 10,
    val watch: Boolean = true,
    val member_limit: Int = 100,
)
