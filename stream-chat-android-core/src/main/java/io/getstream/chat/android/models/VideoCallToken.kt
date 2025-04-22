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
 * A call token that is used in currently available video call integrations.
 *
 * @property token An available call token.
 * @property agoraUid The Uid of Agora.
 * @property agoraAppId The App Id of Agora.
 */
@Deprecated(
    "This third-party library integration is deprecated. Contact to the support team for more information.",
    level = DeprecationLevel.WARNING,
)
@Immutable
public data class VideoCallToken(
    val token: String,
    val agoraUid: Int?,
    val agoraAppId: String?,
)
