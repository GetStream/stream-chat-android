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

package io.getstream.chat.android.client.api2.model.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Deprecated(
    "This third-party library integration is deprecated. Contact the support team for more information.",
    level = DeprecationLevel.WARNING,
)
@JsonClass(generateAdapter = true)
internal data class VideoCallTokenResponse(
    val token: String,
    @field:Json(name = "agora_uid") val agoraUid: Int?,
    @field:Json(name = "agora_app_id") val agoraAppId: String?,
)
