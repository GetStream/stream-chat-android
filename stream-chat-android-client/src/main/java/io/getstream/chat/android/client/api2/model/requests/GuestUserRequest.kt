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

package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class GuestUserRequest(
    val user: GuestUser,
) {

    @JsonClass(generateAdapter = true)
    data class GuestUser(
        val id: String,
        val name: String,
    )

    companion object {
        fun create(id: String, name: String): GuestUserRequest {
            return GuestUserRequest(GuestUser(id = id, name = name))
        }
    }
}
