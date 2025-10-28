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

package io.getstream.chat.android.ui.common.state.messages

import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Reaction

/**
 * Represents the list of actions users can take with selected messages.
 *
 * @property message The selected message.
 */
public sealed class MessageAction {
    public abstract val message: Message
}

/**
 * Add/remove a reaction on a message.
 *
 * @param reaction The reaction to add or remove from the message.
 * @param skipPush If set to "true", skips sending push notification when reacting to a message.
 */
public data class React(
    public val reaction: Reaction,
    override val message: Message,
    public val skipPush: Boolean = false,
) : MessageAction()

/**
 * Retry sending a message.
 */
public data class Resend(
    override val message: Message,
) : MessageAction()

/**
 * Start a message reply.
 */
public data class Reply(
    override val message: Message,
) : MessageAction()

/**
 * Start a thread reply.
 */
public data class ThreadReply(
    override val message: Message,
) : MessageAction()

/**
 * Copy the message content.
 */
public data class Copy(
    override val message: Message,
) : MessageAction()

public data class MarkAsUnread(
    override val message: Message,
) : MessageAction()

/**
 * Start editing an owned message.
 */
public data class Edit(
    override val message: Message,
) : MessageAction()

/**
 * Pins or unpins the message from the channel.
 */
public data class Pin(
    override val message: Message,
) : MessageAction()

/**
 * Show a delete dialog for owned message.
 */
public data class Delete(
    override val message: Message,
) : MessageAction()

/**
 * Show a flag dialog for a message.
 */
public data class Flag(
    override val message: Message,
) : MessageAction()

/**
 * Block the sender of the message.
 */
public data class BlockUser(override val message: Message) : MessageAction()

/**
 * Unblock the sender of the message.
 */
public data class UnblockUser(override val message: Message) : MessageAction()

/**
 * User-customizable action, with any number of extra properties.
 *
 * @param extraProperties Map of key-value pairs that let you store extra data for this action.
 */
public data class CustomAction(
    override val message: Message,
    public val extraProperties: Map<String, Any> = emptyMap(),
) : MessageAction()

public fun MessageAction.updateMessage(message: Message): MessageAction = when (this) {
    is React -> copy(message = message)
    is Resend -> copy(message = message)
    is Reply -> copy(message = message)
    is ThreadReply -> copy(message = message)
    is Copy -> copy(message = message)
    is MarkAsUnread -> copy(message = message)
    is Edit -> copy(message = message)
    is Pin -> copy(message = message)
    is Delete -> copy(message = message)
    is Flag -> copy(message = message)
    is CustomAction -> copy(message = message)
    is BlockUser -> copy(message = message)
    is UnblockUser -> copy(message = message)
}
