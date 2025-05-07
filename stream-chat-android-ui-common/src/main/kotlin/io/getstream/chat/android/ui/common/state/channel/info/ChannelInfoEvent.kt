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

/**
 * Represents events related to channel information actions, such as renaming, muting, hiding, or deleting a channel.
 *
 * This sealed interface defines various event types that can occur during channel operations,
 * including success and error events for specific actions.
 */
@ExperimentalStreamChatApi
public sealed interface ChannelInfoEvent {
    /**
     * Indicates an error occurred while renaming a channel.
     *
     * @param message The error message.
     */
    public data class RenameChannelError(val message: String) : ChannelInfoEvent

    /**
     * Indicates an error occurred while muting a channel.
     *
     * @param message The error message.
     */
    public data class MuteChannelError(val message: String) : ChannelInfoEvent

    /**
     * Indicates an error occurred while unmuting a channel.
     *
     * @param message The error message.
     */
    public data class UnmuteChannelError(val message: String) : ChannelInfoEvent

    /**
     * Indicates an error occurred while hiding a channel.
     *
     * @param message The error message.
     */
    public data class HideChannelError(val message: String) : ChannelInfoEvent

    /**
     * Indicates an error occurred while unhiding a channel.
     *
     * @param message The error message.
     */
    public data class UnhideChannelError(val message: String) : ChannelInfoEvent

    /**
     * Indicates the user successfully left the channel.
     */
    public data object LeaveChannelSuccess : ChannelInfoEvent

    /**
     * Indicates an error occurred while leaving a channel.
     *
     * @param message The error message.
     */
    public data class LeaveChannelError(val message: String) : ChannelInfoEvent

    /**
     * Indicates the channel was successfully deleted.
     */
    public data object DeleteChannelSuccess : ChannelInfoEvent

    /**
     * Indicates an error occurred while deleting a channel.
     *
     * @param message The error message.
     */
    public data class DeleteChannelError(val message: String) : ChannelInfoEvent
}
