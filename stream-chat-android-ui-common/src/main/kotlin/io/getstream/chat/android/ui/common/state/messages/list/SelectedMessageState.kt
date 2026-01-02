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

package io.getstream.chat.android.ui.common.state.messages.list

import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Message

/**
 * Represents a state when a message or its reactions were selected.
 *
 * @property message The selected message.
 * @property ownCapabilities Set of capabilities the user is given for the current channel.
 * For a full list @see [ChannelCapabilities].
 */
public sealed class SelectedMessageState {
    public abstract val message: Message
    public abstract val ownCapabilities: Set<String>
}

/**
 * Represents a state when a message was selected.
 */
public data class SelectedMessageOptionsState(
    override val message: Message,
    override val ownCapabilities: Set<String>,
) : SelectedMessageState()

/**
 * Represents a state when message reactions were selected.
 */
public data class SelectedMessageReactionsState(
    override val message: Message,
    override val ownCapabilities: Set<String>,
) : SelectedMessageState()

/**
 * Represents a state when the show more reactions button was clicked.
 */
public data class SelectedMessageReactionsPickerState(
    override val message: Message,
    override val ownCapabilities: Set<String>,
) : SelectedMessageState()

/**
 * Represents a state when the moderated message was selected.
 */
public data class SelectedMessageFailedModerationState(
    override val message: Message,
    override val ownCapabilities: Set<String>,
) : SelectedMessageState()
