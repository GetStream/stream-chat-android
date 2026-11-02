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

package io.getstream.chat.android.client.internal.state.plugin.logic.querychannels.internal

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ScheduledMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/**
 * Refreshes the channels of the active channel queries whose scheduled messages change, so their
 * [Channel.scheduledMessages] stay current, until the scope is cancelled.
 *
 * @param scheduledMessages The current user's scheduled messages known on the device.
 * @param getQueryChannelsLogic Function returning the logic of the active channel queries.
 */
internal fun CoroutineScope.refreshChannelsOnScheduledMessages(
    scheduledMessages: StateFlow<List<ScheduledMessage>>,
    getQueryChannelsLogic: () -> List<QueryChannelsLogic>,
): Job {
    var previous = emptyMap<String, List<ScheduledMessage>>()
    return scheduledMessages
        .map { it.groupBy(ScheduledMessage::cid) }
        .onEach { current ->
            val changedCids = (previous.keys + current.keys).filter { previous[it] != current[it] }
            previous = current
            if (changedCids.isNotEmpty()) getQueryChannelsLogic().forEach { it.refreshChannelsState(changedCids) }
        }
        .launchIn(this)
}
