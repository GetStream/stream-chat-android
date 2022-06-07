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

package io.getstream.chat.android.common.composer

import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.common.state.MessageAction
import io.getstream.chat.android.common.state.MessageMode
import io.getstream.chat.android.common.state.ValidationError

/**
 * Represents the state within the message input.
 *
 * @param inputValue The current text value that's within the input.
 * @param attachments The currently selected attachments.
 * @param action The currently active [MessageAction].
 * @param validationErrors The list of validation errors.
 * @param mentionSuggestions The list of users that can be used to autocomplete the mention.
 * @param commandSuggestions The list of commands to be displayed in the command suggestion popup.
 * @param coolDownTime The amount of time left until the user is allowed to send the next message.
 * @param messageMode The message mode that's currently active.
 * @param alsoSendToChannel If the message will be shown in the channel after it is sent.
 * @param ownCapabilities Set of capabilities the user is given for the current channel.
 * For a full list @see [io.getstream.chat.android.client.models.ChannelCapabilities].
 */
public data class MessageComposerState(
    val inputValue: String = "",
    val attachments: List<Attachment> = emptyList(),
    val action: MessageAction? = null,
    val validationErrors: List<ValidationError> = emptyList(),
    val mentionSuggestions: List<User> = emptyList(),
    val commandSuggestions: List<Command> = emptyList(),
    val coolDownTime: Int = 0,
    val messageMode: MessageMode = MessageMode.Normal,
    val alsoSendToChannel: Boolean = false,
    val ownCapabilities: Set<String> = setOf()
)
