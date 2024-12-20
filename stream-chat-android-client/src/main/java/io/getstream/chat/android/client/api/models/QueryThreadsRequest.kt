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

package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.models.User

/**
 * Query threads request.
 *
 * @property watch If true, all the channels corresponding to threads returned in response will be watched.
 * Defaults to true.
 * @property limit The number of threads to return. Defaults to 10.
 * @property memberLimit The number of members to request per thread. Defaults to 100.
 * @property next The next pagination token. This token can be used to fetch the next page of threads.
 * @property participantLimit The number of thread participants to request per thread. Defaults to 100.
 * @property prev The previous pagination token. This token can be used to fetch the previous page of threads.
 * @property replyLimit The number of latest replies to fetch per thread. Defaults to 2.
 * @property user The user for which the threads are queried. Defaults to null.
 * @property userId The user ID for which the threads are queried. Defaults to null.
 */
public data class QueryThreadsRequest @JvmOverloads constructor(
    public val watch: Boolean = true,
    public val limit: Int = 10,
    public val memberLimit: Int = 100,
    public val next: String? = null,
    public val participantLimit: Int = 100,
    public val prev: String? = null,
    public val replyLimit: Int = 2,
    public val user: User? = null,
    public val userId: String? = null,
)
