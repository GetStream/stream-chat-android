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

package io.getstream.chat.android.ui.common.state.messages.list

import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Message

/**
 * Represents a state when a message or its reactions were selected.
 *
 * @param message The selected message.
 * @param ownCapabilities Set of capabilities the user is given for the current channel.
 * For a full list @see [ChannelCapabilities].
 */
public sealed class SelectedMessageState(public val message: Message, public val ownCapabilities: Set<String>)

/**
 * Represents a state when a message was selected.
 */
public class SelectedMessageOptionsState(message: Message, ownCapabilities: Set<String>) :
    SelectedMessageState(message, ownCapabilities)

/**
 * Represents a state when message reactions were selected.
 */
public class SelectedMessageReactionsState(message: Message, ownCapabilities: Set<String>) :
    SelectedMessageState(message, ownCapabilities)

/**
 * Represents a state when the show more reactions button was clicked.
 */
public class SelectedMessageReactionsPickerState(message: Message, ownCapabilities: Set<String>) :
    SelectedMessageState(message, ownCapabilities)

/**
 * Represents a state when the moderated message was selected.
 */
public class SelectedMessageFailedModerationState(message: Message, ownCapabilities: Set<String>) :
    SelectedMessageState(message, ownCapabilities)
