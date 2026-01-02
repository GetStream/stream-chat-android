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

package io.getstream.chat.android.models

import androidx.compose.runtime.Immutable

/**
 * Represents Agora channel information that contains available [channel] in a chat channel.
 *
 * @property channel A new channel.
 */
@Deprecated(
    "This third-party library integration is deprecated. Contact the support team for more information.",
    level = DeprecationLevel.WARNING,
)
@Immutable
public data class AgoraChannel(val channel: String)
