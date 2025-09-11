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
import io.getstream.chat.android.client.api2.model.dto.UpstreamUserDto

/**
 * Used for querying threads.
 * @see [ThreadsApi.queryThreads]
 *
 * @param filter The filter conditions for the query.
 * @param sort The sort conditions for the query.
 * @param watch If true, all the channels corresponding to threads returned in response will be watched.
 * @param limit The number of threads to return. Defaults to 10. Max limit is 25.
 * @param member_limit The number of members to request per thread. Defaults to 100. Max limit is 100.
 * @param next The next pagination token. This token can be used to fetch the next page of threads.
 * @param participant_limit The number of thread participants to request per thread. Defaults to 100. Max limit is 100.
 * @param prev The previous pagination token. This token can be used to fetch the previous page of threads.
 * @param reply_limit The number of latest replies to fetch per thread. Defaults to 2. Max limit is 10.
 * @param user The user for which the threads are queried. Defaults to null.
 * @param user_id The user ID for which the threads are queried. Defaults to null.
 */
@JsonClass(generateAdapter = true)
internal data class QueryThreadsRequest(
    val filter: Map<*, *>? = null,
    val sort: List<Map<String, Any>>? = null,
    val watch: Boolean = true,
    val limit: Int = 10,
    val member_limit: Int = 100,
    val next: String? = null,
    val participant_limit: Int = 100,
    val prev: String? = null,
    val reply_limit: Int = 2,
    val user: UpstreamUserDto? = null,
    val user_id: String? = null,
)
