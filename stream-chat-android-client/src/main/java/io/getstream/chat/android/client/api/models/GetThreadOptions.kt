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

package io.getstream.chat.android.client.api.models

/**
 * Query threads request.
 *
 * @property watch If true, all the channels corresponding to threads returned in response will be watched.
 * Defaults to true.
 * @property replyLimit The number of latest replies to fetch per thread. Defaults to 2.
 * @property participantLimit The number of thread participants to request per thread. Defaults to 100.
 * @property memberLimit The number of members to request per thread. Defaults to 100.
 */
public data class GetThreadOptions @JvmOverloads constructor(
    public val watch: Boolean = true,
    public val replyLimit: Int = 2,
    public val participantLimit: Int = 100,
    public val memberLimit: Int = 100,
) {

    internal fun toMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        map["watch"] = watch.toString()
        map["reply_limit"] = replyLimit.toString()
        map["participant_limit"] = participantLimit.toString()
        map["member_limit"] = memberLimit.toString()
        return map
    }
}
