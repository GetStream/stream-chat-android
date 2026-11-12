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

package io.getstream.chat.android.ui.common.state.messages.composer

import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.Message
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * What the composer knows to schedule its message in the current channel.
 *
 * The counts come from the scheduled messages known on this device, so they can be lower than the server's. The server
 * then refuses the message itself.
 *
 * @param canSchedule Whether the current user has the [ChannelCapabilities.SCHEDULE_MESSAGE] capability in the
 * channel.
 * @param pendingInChannel The number of pending scheduled messages of the current user in the channel, including its
 * threads. A failed message, or one whose date has passed, does not count.
 * @param maxPerChannel The maximum number of pending scheduled messages per channel, from
 * [Config.scheduledMessagesMaxPerChannel], or null when it is not known.
 * @param pendingForUser The number of pending scheduled messages of the current user in all channels.
 * @param maxPerUser The maximum number of pending scheduled messages per user, from
 * [Config.scheduledMessagesMaxPerUser], or null when it is not known.
 * @param minLeadTimeSeconds How far ahead, in seconds, a message is scheduled at least.
 * @param maxLeadTimeSeconds How far ahead, in seconds, a message can be scheduled at most.
 * @param hasCommands Whether the channel has commands, so a message that starts with `/` can't be scheduled.
 */
public data class ScheduleMessageState(
    val canSchedule: Boolean = false,
    val pendingInChannel: Int = 0,
    val maxPerChannel: Int? = null,
    val pendingForUser: Int = 0,
    val maxPerUser: Int? = null,
    val minLeadTimeSeconds: Int = DEFAULT_MIN_LEAD_TIME_SECONDS,
    val maxLeadTimeSeconds: Int = DEFAULT_MAX_LEAD_TIME_SECONDS,
    val hasCommands: Boolean = false,
) {

    /**
     * Whether the channel already has [maxPerChannel] pending scheduled messages, so no new message can be scheduled.
     */
    val isChannelLimitReached: Boolean
        get() = maxPerChannel != null && pendingInChannel >= maxPerChannel

    /**
     * Whether the current user already has [maxPerUser] pending scheduled messages, so no new message can be
     * scheduled.
     */
    val isUserLimitReached: Boolean
        get() = maxPerUser != null && pendingForUser >= maxPerUser

    /**
     * Why [message] can't be scheduled as a new message in the channel, whatever its date, or null when it can.
     *
     * @param message The message to schedule, as built by the composer.
     */
    public fun messageError(message: Message): ScheduleMessageError? = when {
        hasCommands && message.text.startsWith("/") -> ScheduleMessageError.CommandNotAllowed
        maxPerChannel != null && isChannelLimitReached -> ScheduleMessageError.ChannelLimitReached(maxPerChannel)
        maxPerUser != null && isUserLimitReached -> ScheduleMessageError.UserLimitReached(maxPerUser)
        else -> null
    }

    /**
     * Why a message can't be scheduled at [date], or null when it can.
     *
     * @param date The date to schedule the message at.
     * @param now The current date.
     */
    public fun dateError(date: Date, now: Date = Date()): ScheduleMessageError? = when {
        date.time < now.time + TimeUnit.SECONDS.toMillis(minLeadTimeSeconds.toLong()) ->
            ScheduleMessageError.TooSoon(minLeadTimeSeconds)

        date.time > now.time + TimeUnit.SECONDS.toMillis(maxLeadTimeSeconds.toLong()) ->
            ScheduleMessageError.TooFar(maxLeadTimeSeconds)

        else -> null
    }
}

/** One minute, the server default. */
private const val DEFAULT_MIN_LEAD_TIME_SECONDS = 60

/** 120 days, the server default. */
private const val DEFAULT_MAX_LEAD_TIME_SECONDS = 120 * 24 * 60 * 60

/**
 * Why a message can't be scheduled. More kinds can be added in a minor version, so a `when` on it needs an `else`
 * branch.
 */
public abstract class ScheduleMessageError internal constructor() {

    /**
     * The date is less than [minLeadTimeSeconds] from now.
     *
     * @property minLeadTimeSeconds How far ahead, in seconds, a message is scheduled at least.
     */
    public data class TooSoon(val minLeadTimeSeconds: Int) : ScheduleMessageError()

    /**
     * The date is more than [maxLeadTimeSeconds] from now.
     *
     * @property maxLeadTimeSeconds How far ahead, in seconds, a message can be scheduled at most.
     */
    public data class TooFar(val maxLeadTimeSeconds: Int) : ScheduleMessageError()

    /**
     * The channel already has [limit] pending scheduled messages of the current user.
     *
     * @property limit The maximum number of pending scheduled messages per channel.
     */
    public data class ChannelLimitReached(val limit: Int) : ScheduleMessageError()

    /**
     * The current user already has [limit] pending scheduled messages in all channels.
     *
     * @property limit The maximum number of pending scheduled messages per user.
     */
    public data class UserLimitReached(val limit: Int) : ScheduleMessageError()

    /**
     * The message starts with a `/` command, which can't be scheduled in a channel with commands.
     */
    public data object CommandNotAllowed : ScheduleMessageError()

    /**
     * The message could not be scheduled for another reason.
     *
     * @property error The error of the request.
     */
    public data class Failed(val error: io.getstream.result.Error) : ScheduleMessageError()
}
