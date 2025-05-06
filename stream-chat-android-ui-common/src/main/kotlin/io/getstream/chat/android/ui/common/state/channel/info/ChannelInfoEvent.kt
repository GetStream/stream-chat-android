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

package io.getstream.chat.android.ui.common.state.channel.info

import io.getstream.chat.android.core.ExperimentalStreamChatApi

@ExperimentalStreamChatApi
public sealed interface ChannelInfoEvent {
    public data class UpdateNameError(val message: String) : ChannelInfoEvent
    public data class MuteError(val message: String) : ChannelInfoEvent
    public data class UnmuteError(val message: String) : ChannelInfoEvent
    public data class HideError(val message: String) : ChannelInfoEvent
    public data class UnhideError(val message: String) : ChannelInfoEvent
    public data object LeaveSuccess : ChannelInfoEvent
    public data class LeaveError(val message: String) : ChannelInfoEvent
    public data object DeleteSuccess : ChannelInfoEvent
    public data class DeleteError(val message: String) : ChannelInfoEvent
}
