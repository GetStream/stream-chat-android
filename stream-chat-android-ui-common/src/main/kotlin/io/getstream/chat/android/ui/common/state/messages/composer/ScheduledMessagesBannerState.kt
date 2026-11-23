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

import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.models.ScheduledMessageStatus
import io.getstream.chat.android.ui.common.feature.messages.composer.scheduled.isPending
import java.util.Date

/**
 * What the scheduled messages banner of a channel or a thread shows. It shows one state at a time: [Failed] first,
 * then [LimitReached], then [Scheduled]. More states can be added in a minor version, so a `when` on it needs an
 * `else` branch.
 */
public abstract class ScheduledMessagesBannerState internal constructor() {

    /**
     * The ID of the thread whose scheduled replies the banner is about, and whose scheduled messages it opens, or null
     * for the scheduled messages of the channel, thread replies included.
     */
    public abstract val parentMessageId: String?

    /**
     * Some scheduled messages of the channel or the thread failed to send.
     *
     * @property count The number of failed scheduled messages.
     * @property parentMessageId The ID of the thread of the failed replies, or null for the channel, thread replies
     * included.
     */
    public data class Failed(
        val count: Int,
        override val parentMessageId: String? = null,
    ) : ScheduledMessagesBannerState()

    /**
     * The channel has as many pending scheduled messages as it allows, so no new message can be scheduled in it. It is
     * about the whole channel, also when shown in a thread, so [parentMessageId] is always null.
     *
     * @property count The number of pending scheduled messages of the current user in the channel, including its
     * threads.
     * @property limit The maximum number of pending scheduled messages per channel.
     */
    public data class LimitReached(val count: Int, val limit: Int) : ScheduledMessagesBannerState() {
        override val parentMessageId: String? get() = null
    }

    /**
     * The channel or the thread has scheduled messages that are not sent yet.
     *
     * @property count The number of scheduled messages that did not fail and whose date has not passed.
     * @property nextScheduledAt The date of the next one to be sent.
     * @property parentMessageId The ID of the thread of the scheduled replies, or null for the channel, thread
     * replies included.
     */
    public data class Scheduled(
        val count: Int,
        val nextScheduledAt: Date,
        override val parentMessageId: String? = null,
    ) : ScheduledMessagesBannerState() {

        /** Whether they are replies of a thread. */
        val isInThread: Boolean get() = parentMessageId != null
    }
}

/**
 * The banner for the [scheduledMessages] of a channel or a thread, or null when the user can't schedule messages in
 * the channel or there is nothing to show.
 *
 * @param scheduledMessages The scheduled messages of the channel or the thread.
 * @param scheduleMessageState The scheduling state of the channel, for the capability and the channel limit.
 * @param parentMessageId The ID of the thread when [scheduledMessages] are its replies, or null for the channel,
 * thread replies included.
 * @param now The current date. Messages whose date has passed are not shown as scheduled.
 */
internal fun scheduledMessagesBannerState(
    scheduledMessages: List<ScheduledMessage>,
    scheduleMessageState: ScheduleMessageState,
    parentMessageId: String?,
    now: Date,
): ScheduledMessagesBannerState? {
    if (!scheduleMessageState.canSchedule) return null
    val failed = scheduledMessages.filter { it.status == ScheduledMessageStatus.Failed }
    val scheduled = scheduledMessages.filter { it.isPending(now) }
    val maxPerChannel = scheduleMessageState.maxPerChannel
    return when {
        failed.isNotEmpty() -> ScheduledMessagesBannerState.Failed(failed.size, parentMessageId)
        maxPerChannel != null && scheduleMessageState.isChannelLimitReached ->
            ScheduledMessagesBannerState.LimitReached(scheduleMessageState.pendingInChannel, maxPerChannel)
        scheduled.isNotEmpty() -> ScheduledMessagesBannerState.Scheduled(
            count = scheduled.size,
            nextScheduledAt = scheduled.minOf(ScheduledMessage::scheduledAt),
            parentMessageId = parentMessageId,
        )
        else -> null
    }
}
