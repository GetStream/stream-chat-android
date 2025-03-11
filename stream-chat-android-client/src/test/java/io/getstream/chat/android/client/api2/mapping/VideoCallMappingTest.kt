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

package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.Mother
import io.getstream.chat.android.models.AgoraChannel
import io.getstream.chat.android.models.HMSRoom
import io.getstream.chat.android.models.VideoCallInfo
import io.getstream.chat.android.models.VideoCallToken
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class VideoCallMappingTest {

    @Test
    fun `AgoraDto is correctly mapped to AgoraChannel`() {
        val agoraDto = Mother.randomAgoraDto()
        val agoraChannel = agoraDto.toDomain()
        val expected = AgoraChannel(agoraDto.channel)
        agoraChannel shouldBeEqualTo expected
    }

    @Test
    fun `HMSDto is correctly mapped to HMSRoom`() {
        val hmsDto = Mother.randomHMSDto()
        val hmsRoom = hmsDto.toDomain()
        val expected = HMSRoom(hmsDto.roomId, hmsDto.roomName)
        hmsRoom shouldBeEqualTo expected
    }

    @Test
    fun `CreateVideoCallResponse is correctly mapped to VideoCallInfo`() {
        val createVideoCallResponse = Mother.randomCreateVideoCallResponse()
        val videoCallInfo = createVideoCallResponse.toDomain()
        val expected = VideoCallInfo(
            callId = createVideoCallResponse.call.id,
            provider = createVideoCallResponse.call.provider,
            type = createVideoCallResponse.call.type,
            agoraChannel = createVideoCallResponse.call.agora.toDomain(),
            hmsRoom = createVideoCallResponse.call.hms.toDomain(),
            videoCallToken = VideoCallToken(
                token = createVideoCallResponse.token,
                agoraUid = createVideoCallResponse.agoraUid,
                agoraAppId = createVideoCallResponse.agoraAppId,
            ),
        )
        videoCallInfo shouldBeEqualTo expected
    }

    @Test
    fun `VideoCallTokenResponse is correctly mapped to VideoCallToken`() {
        val videoCallTokenResponse = Mother.randomVideoCallTokenResponse()
        val videoCallToken = videoCallTokenResponse.toDomain()
        val expected = VideoCallToken(
            token = videoCallTokenResponse.token,
            agoraUid = videoCallTokenResponse.agoraUid,
            agoraAppId = videoCallTokenResponse.agoraAppId,
        )
        videoCallToken shouldBeEqualTo expected
    }
}
