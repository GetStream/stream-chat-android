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

package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.AgoraDto
import io.getstream.chat.android.client.api2.model.dto.HMSDto
import io.getstream.chat.android.client.api2.model.response.CreateVideoCallResponse
import io.getstream.chat.android.client.api2.model.response.VideoCallTokenResponse
import io.getstream.chat.android.models.AgoraChannel
import io.getstream.chat.android.models.HMSRoom
import io.getstream.chat.android.models.VideoCallInfo
import io.getstream.chat.android.models.VideoCallToken

@Deprecated(
    "This third-party library integration is deprecated. Contact to the support team for more information.",
    level = DeprecationLevel.WARNING,
)
internal fun AgoraDto.toDomain(): AgoraChannel {
    return AgoraChannel(channel = channel)
}

@Deprecated(
    "This third-party library integration is deprecated. Contact to the support team for more information.",
    level = DeprecationLevel.WARNING,
)
internal fun HMSDto.toDomain(): HMSRoom {
    return HMSRoom(roomId = roomId, roomName = roomName)
}

@Deprecated(
    "This third-party library integration is deprecated. Contact to the support team for more information.",
    level = DeprecationLevel.WARNING,
)
@Suppress("DEPRECATION")
internal fun CreateVideoCallResponse.toDomain(): VideoCallInfo {
    return VideoCallInfo(
        callId = call.id,
        provider = call.provider,
        type = call.type,
        agoraChannel = call.agora.toDomain(),
        hmsRoom = call.hms.toDomain(),
        videoCallToken = VideoCallToken(
            token = token,
            agoraUid = agoraUid,
            agoraAppId = agoraAppId,
        ),
    )
}

@Deprecated(
    "This third-party library integration is deprecated. Contact to the support team for more information.",
    level = DeprecationLevel.WARNING,
)
internal fun VideoCallTokenResponse.toDomain(): VideoCallToken {
    return VideoCallToken(
        token = token,
        agoraUid = agoraUid,
        agoraAppId = agoraAppId,
    )
}
