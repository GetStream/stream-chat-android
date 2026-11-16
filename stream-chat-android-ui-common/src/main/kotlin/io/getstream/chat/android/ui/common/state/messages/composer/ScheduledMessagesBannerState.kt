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
import java.util.Date

/**
 * What the scheduled messages banner of a channel or a thread shows. It shows one state at a time: [Failed] first,
 * then [LimitReached], then [Scheduled]. More states can be added in a minor version, so a `when` on it needs an
 * `else` branch.
 */
public abstract class ScheduledMessagesBannerState internal constructor() {

    /**
     * Some scheduled messages of the channel or the thread failed to send.
     *
     * @property count The number of failed scheduled messages.
     */
    public data class Failed(val count: Int) : ScheduledMessagesBannerState()

    /**
     * The channel has as many pending scheduled messages as it allows, so no new message can be scheduled in it.
     *
     * @property count The number of pending scheduled messages of the current user in the channel, including its
     * threads.
     * @property limit The maximum number of pending scheduled messages per channel.
     */
    public data class LimitReached(val count: Int, val limit: Int) : ScheduledMessagesBannerState()

    /**
     * The channel or the thread has scheduled messages that are not sent yet.
     *
     * @property count The number of scheduled messages that did not fail.
     * @property nextScheduledAt The date of the next one to be sent.
     * @property isInThread Whether they are replies of a thread.
     */
    public data class Scheduled(
        val count: Int,
        val nextScheduledAt: Date,
        val isInThread: Boolean,
    ) : ScheduledMessagesBannerState()
}

/**
 * The banner for the [scheduledMessages] of a channel or a thread, or null when the user can't schedule messages in
 * the channel or there is nothing to show.
 *
 * @param scheduledMessages The scheduled messages of the channel or the thread.
 * @param scheduleMessageState The scheduling state of the channel, for the capability and the channel limit.
 * @param isInThread Whether [scheduledMessages] are the replies of a thread.
 */
internal fun scheduledMessagesBannerState(
    scheduledMessages: List<ScheduledMessage>,
    scheduleMessageState: ScheduleMessageState,
    isInThread: Boolean,
): ScheduledMessagesBannerState? {
    if (!scheduleMessageState.canSchedule) return null
    val (failed, scheduled) = scheduledMessages.partition { it.status == ScheduledMessageStatus.Failed }
    val maxPerChannel = scheduleMessageState.maxPerChannel
    return when {
        failed.isNotEmpty() -> ScheduledMessagesBannerState.Failed(failed.size)
        maxPerChannel != null && scheduleMessageState.isChannelLimitReached ->
            ScheduledMessagesBannerState.LimitReached(scheduleMessageState.pendingInChannel, maxPerChannel)
        scheduled.isNotEmpty() -> ScheduledMessagesBannerState.Scheduled(
            count = scheduled.size,
            nextScheduledAt = scheduled.minOf(ScheduledMessage::scheduledAt),
            isInThread = isInThread,
        )
        else -> null
    }
}
