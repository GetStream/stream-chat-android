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

package io.getstream.chat.android.compose.viewmodel.scheduled

import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.ui.scheduled.ScheduledMessageEdit
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.feature.scheduled.ScheduledMessageListController
import io.getstream.chat.android.ui.common.state.scheduled.ScheduledMessageListEvent
import io.getstream.chat.android.ui.common.state.scheduled.ScheduledMessageListState
import io.getstream.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date

/**
 * ViewModel of a list of the current user's scheduled messages. It loads them when connected, and follows their
 * changes.
 *
 * @param controller The [ScheduledMessageListController] handling the business logic and the state of the list.
 * @param user The currently logged in user.
 * @param connectionState The state of the connection.
 * @param chatClient The [ChatClient] holding the scheduled messages, for the composer that edits them.
 */
public class ScheduledMessageListViewModel internal constructor(
    private val controller: ScheduledMessageListController,
    public val user: StateFlow<User?>,
    public val connectionState: StateFlow<ConnectionState>,
    internal val chatClient: ChatClient,
) : ViewModel(controller) {

    /**
     * The state of the list.
     */
    public val state: StateFlow<ScheduledMessageListState> = controller.state

    /**
     * The results of the actions to tell the user.
     */
    public val events: Flow<ScheduledMessageListEvent> = controller.events

    /**
     * Loads the scheduled messages again, after they failed to load.
     */
    public fun load() {
        controller.load()
    }

    /**
     * Changes the date the scheduled message with [id] is sent at.
     *
     * @param id The ID of the scheduled message.
     * @param scheduledAt The new date.
     */
    public fun reschedule(id: String, scheduledAt: Date) {
        controller.reschedule(id, scheduledAt)
    }

    /**
     * Sends the scheduled message with [id] right away.
     *
     * @param id The ID of the scheduled message.
     */
    public fun sendNow(id: String) {
        controller.sendNow(id)
    }

    /**
     * Deletes the scheduled message with [id], so it is never sent.
     *
     * @param id The ID of the scheduled message.
     */
    public fun delete(id: String) {
        controller.delete(id)
    }

    /**
     * The config of the channel with [cid], as saved on this device when this is called.
     */
    internal fun channelConfig(cid: String): Config? = controller.channelConfig(cid)

    /**
     * The scheduled message edited in the composer of the list, kept across configuration changes.
     */
    internal val edit: ScheduledMessageEdit = ScheduledMessageEdit()

    /**
     * The content of the scheduled message with [id] to edit in a composer, or null when it is no longer there.
     */
    internal suspend fun messageToEdit(id: String): Message? = controller.messageToEdit(id)

    /**
     * Saves [message], built by a composer, as the new content of the scheduled message with [id], and keeps its
     * date. Calls [onResult] with the scheduled message as saved locally, or with the reason it was refused.
     */
    internal fun edit(
        id: String,
        message: Message,
        isQuoteRemoved: Boolean,
        resolveAttachments: suspend (List<Attachment>) -> List<Attachment>,
        onResult: (Result<ScheduledMessage>) -> Unit,
    ) {
        controller.edit(id, message, isQuoteRemoved, resolveAttachments, onResult)
    }

    /**
     * Schedules [message], built by a composer, at [scheduledAt] as a new message that replaces the scheduled message
     * with [id], which the server failed to send. Calls [onResult] with the new scheduled message as saved locally,
     * or with the reason it was refused.
     */
    @Suppress("LongParameterList")
    internal fun editAndReschedule(
        id: String,
        message: Message,
        isQuoteRemoved: Boolean,
        scheduledAt: Date,
        resolveAttachments: suspend (List<Attachment>) -> List<Attachment>,
        onResult: (Result<ScheduledMessage>) -> Unit,
    ) {
        controller.editAndReschedule(id, message, isQuoteRemoved, scheduledAt, resolveAttachments, onResult)
    }
}
