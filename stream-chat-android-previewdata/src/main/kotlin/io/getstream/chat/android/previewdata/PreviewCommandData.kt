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

package io.getstream.chat.android.previewdata

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Command

@InternalStreamChatApi
public object PreviewCommandData {

    public val command1: Command = Command(
        name = "giphy",
        description = "Post a random gif to the channel",
        args = "[text]",
        set = "fun_set",
    )
    public val command2: Command = Command(
        name = "mute",
        description = "Mute a user",
        args = "[@username]",
        set = "moderation_set",
    )
    public val command3: Command = Command(
        name = "unmute",
        description = "Unmute a user",
        args = "[@username]",
        set = "moderation_set",
    )
}
