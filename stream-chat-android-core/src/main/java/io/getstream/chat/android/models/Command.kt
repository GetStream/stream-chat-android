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
import io.getstream.chat.android.core.internal.InternalStreamChatApi

@Immutable
public data class Command(
    val name: String,
    val description: String,
    val args: String,
    val set: String,
)

/**
 * Contains the default command names supported by Stream Chat.
 */
@InternalStreamChatApi
public object CommandDefaults {

    /**
     * Command for muting a user.
     */
    @InternalStreamChatApi
    public const val MUTE: String = "mute"

    /**
     * Command for unmuting a user.
     */
    @InternalStreamChatApi
    public const val UNMUTE: String = "unmute"

    /**
     * Command for posting a Giphy message.
     */
    @InternalStreamChatApi
    public const val GIPHY: String = "giphy"
}
