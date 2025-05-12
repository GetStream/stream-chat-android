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

package io.getstream.chat.android.ui.common.feature.channel.info

import io.getstream.chat.android.core.ExperimentalStreamChatApi

/**
 * Represents side-effect events related to channel information actions.
 */
@ExperimentalStreamChatApi
public sealed interface ChannelInfoViewEvent {

    /**
     * Represents an error event occurred while performing an action.
     */
    public sealed interface Error : ChannelInfoViewEvent

    /**
     * Indicates an error occurred while renaming a channel.
     */
    public data object RenameChannelError : Error

    /**
     * Indicates an error occurred while muting a channel.
     */
    public data object MuteChannelError : Error

    /**
     * Indicates an error occurred while unmuting a channel.
     */
    public data object UnmuteChannelError : Error

    /**
     * Indicates an error occurred while hiding a channel.
     */
    public data object HideChannelError : Error

    /**
     * Indicates an error occurred while unhiding a channel.
     */
    public data object UnhideChannelError : Error

    /**
     * Indicates an error occurred while leaving a channel.
     */
    public data object LeaveChannelError : Error

    /**
     * Indicates an error occurred while deleting a channel.
     */
    public data object DeleteChannelError : Error

    /**
     * Indicates the user successfully left the channel.
     */
    public data object LeaveChannelSuccess : ChannelInfoViewEvent

    /**
     * Indicates the channel was successfully deleted.
     */
    public data object DeleteChannelSuccess : ChannelInfoViewEvent
}
