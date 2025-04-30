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

import androidx.compose.runtime.Immutable

/**
 * Represents currently available call information for Agora and HMS video calls.
 *
 * @property callId The call id, which indicates a dedicated video call id on the channel.
 * @property provider The provider.
 * @property type The call type.
 * @property agoraChannel The available channel info of Agora.
 * @property hmsRoom The available room info of HMS.
 * @property videoCallToken The token info of the video call.
 */
@Deprecated(
    "This third-party library integration is deprecated. Contact the support team for more information.",
    level = DeprecationLevel.WARNING,
)
@Immutable
public data class VideoCallInfo(
    val callId: String,
    val provider: String,
    val type: String,
    val agoraChannel: AgoraChannel,
    val hmsRoom: HMSRoom,
    val videoCallToken: VideoCallToken,
)
