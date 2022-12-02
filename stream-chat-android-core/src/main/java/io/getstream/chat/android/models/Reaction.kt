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

package io.getstream.chat.android.models

import java.util.Date

public data class Reaction(
    var messageId: String = "",
    var type: String = "",
    var score: Int = 0,
    var user: User? = null,
    var userId: String = "",
    var createdAt: Date? = null,

    var updatedAt: Date? = null,

    var deletedAt: Date? = null,

    var syncStatus: SyncStatus = SyncStatus.COMPLETED,

    override var extraData: MutableMap<String, Any> = mutableMapOf(),

    var enforceUnique: Boolean = false,

) : CustomObject {
    // this is a workaround around a backend issue
    // for some reason we sometimes only get the user id and not the user object
    // this needs more investigation on the backend side of things
    public fun fetchUserId(): String {
        return user?.id ?: userId
    }
}
