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

package io.getstream.chat.android.client.scheduled

import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.ScheduledMessageStatus
import io.getstream.result.Error
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Refuses on the device what the server would reject, before a change of a scheduled message is saved. The checks
 * use the channel config and messages saved on this device, and pass when they are not known.
 *
 * @param getRepositoryFacade Function to provide the [RepositoryFacade] of the connected user.
 */
internal class ScheduledMessageValidator(
    private val getRepositoryFacade: () -> RepositoryFacade,
    private val now: () -> Date,
) {

    /**
     * Refuses a slash command in a channel with commands, which the server never schedules, and a quote of a deleted
     * message.
     */
    suspend fun validateContent(cid: String, message: Message): Error? {
        if (message.text.startsWith("/") && config(cid)?.commands.orEmpty().isNotEmpty()) {
            return Error.GenericError("Slash commands can't be scheduled.")
        }
        return validateQuote(message.replyMessageId)
    }

    suspend fun validateQuote(replyMessageId: String?): Error? {
        val quoted = replyMessageId?.let { getRepositoryFacade().selectMessage(it) }
        return if (quoted?.deletedAt != null) {
            Error.GenericError("The quoted message was deleted. Remove the quote and try again.")
        } else {
            null
        }
    }

    /**
     * Refuses a date further ahead than [Config.scheduledMessagesMaxLeadTimeSeconds], as the server does: once rounded
     * up to the next minute, and with one minute of slack.
     */
    suspend fun validateScheduledAt(cid: String, scheduledAt: Date): Error? {
        val maxLeadTime = config(cid)?.scheduledMessagesMaxLeadTimeSeconds ?: return null
        val roundedUp = (scheduledAt.time + MINUTE_MILLIS - 1) / MINUTE_MILLIS * MINUTE_MILLIS
        val latest = now().time + TimeUnit.SECONDS.toMillis(maxLeadTime.toLong()) + MINUTE_MILLIS
        return if (roundedUp > latest) {
            Error.GenericError("A message can be scheduled at most $maxLeadTime seconds ahead.")
        } else {
            null
        }
    }

    /**
     * Refuses a new scheduled message in [cid] once the pending ones among [others] reach
     * [Config.scheduledMessagesMaxPerChannel] in the channel, or [Config.scheduledMessagesMaxPerUser] in all channels.
     * The ones shown as failed don't count, and neither do the ones whose date has passed, as the server has most
     * likely sent them. The count can be lower than the server's, which then refuses the message itself.
     */
    suspend fun validateLimits(cid: String, others: Collection<LocalScheduledMessage>): Error? {
        val config = config(cid) ?: return null
        val now = now()
        val pending = others.filter {
            !it.isDeleted &&
                it.scheduledAt.after(now) &&
                it.toScheduledMessage().status != ScheduledMessageStatus.Failed
        }
        val maxPerChannel = config.scheduledMessagesMaxPerChannel
        if (maxPerChannel != null && pending.count { it.cid == cid } >= maxPerChannel) {
            return Error.GenericError("The channel has the maximum of $maxPerChannel scheduled messages.")
        }
        val maxPerUser = config.scheduledMessagesMaxPerUser
        if (maxPerUser != null && pending.size >= maxPerUser) {
            return Error.GenericError("You have the maximum of $maxPerUser scheduled messages.")
        }
        return null
    }

    private suspend fun config(cid: String): Config? =
        getRepositoryFacade().selectChannelConfig(cid.cidToTypeAndId().first)?.config
}

/** One minute, the precision of the dates the server sends messages at. */
private const val MINUTE_MILLIS = 60_000L
