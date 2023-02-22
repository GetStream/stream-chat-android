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

package io.getstream.chat.android.common.state

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction

/**
 * Represents the list of actions users can take with selected messages.
 *
 * @param message The selected message.
 */
public sealed class MessageAction(public val message: Message)

/**
 * Add/remove a reaction on a message.
 *
 * @param reaction The reaction to add or remove from the message.
 */
public class React(
    public val reaction: Reaction,
    message: Message,
) : MessageAction(message)

/**
 * Retry sending a message.
 *
 * @param message The message to be resent.
 * @param skipPush If the message should skip triggering a push notification when sent. False by default.
 * @param skipEnrichUrl If the message should skip enriching the URL. If URL is not enriched, it will not be
 */
public class Resend(
    message: Message,
    public val skipPush: Boolean = false,
    public val skipEnrichUrl: Boolean = false,
) : MessageAction(message)

/**
 * Start a message reply.
 */
public class Reply(message: Message) : MessageAction(message)

/**
 * Start a thread reply.
 */
public class ThreadReply(message: Message) : MessageAction(message)

/**
 * Copy the message content.
 */
public class Copy(message: Message) : MessageAction(message)

/**
 * Start editing an owned message.
 */
public class Edit(message: Message) : MessageAction(message)

/**
 * Pins or unpins the message from the channel.
 */
public class Pin(message: Message) : MessageAction(message)

/**
 * Show a delete dialog for owned message.
 */
public class Delete(message: Message) : MessageAction(message)

/**
 * Show a flag dialog for a message.
 */
public class Flag(message: Message) : MessageAction(message)

/**
 * Mutes or unmutes the user who sent the message.
 */
@Deprecated(
    message = "The ability to mute a user via a message option has been deprecated and will be removed.",
    level = DeprecationLevel.ERROR,
)
public class MuteUser(message: Message) : MessageAction(message)

/**
 * User-customizable action, with any number of extra properties.
 *
 * @param extraProperties Map of key-value pairs that let you store extra data for this action.
 */
public class CustomAction(
    message: Message,
    public val extraProperties: Map<String, Any> = emptyMap(),
) : MessageAction(message)
