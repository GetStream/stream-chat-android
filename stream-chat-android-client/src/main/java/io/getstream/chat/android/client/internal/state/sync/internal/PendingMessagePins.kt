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

package io.getstream.chat.android.client.internal.state.sync.internal

import io.getstream.chat.android.client.errors.isPermanent
import io.getstream.chat.android.client.internal.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.log.taggedLogger
import io.getstream.result.Error
import io.getstream.result.Result
import java.util.Date
import java.util.concurrent.ConcurrentHashMap

/**
 * Keeps the pins and unpins that failed with a temporary network error while the network was not available, so they
 * show at once and are sent again on the next connection. A failure while the network is available is left to the
 * caller, as before.
 *
 * Only the last pin or unpin of a message is kept. It is sent again as a partial update with only the pin fields.
 * Kept in memory only, so they are not sent if the process ends first. The local change is not stored either, so
 * the next start shows the server value.
 *
 * @param sendPin Sends the pin fields of a message to the server.
 * @param getCurrentUser Returns the current user, shown as the user who pinned the message.
 * @param isNetworkAvailable Returns whether the device has a network connection.
 */
internal class PendingMessagePins(
    private val logic: LogicRegistry,
    private val sendPin: suspend (messageId: String, set: Map<String, Any>) -> Result<Message>,
    private val getCurrentUser: () -> User?,
    private val isNetworkAvailable: () -> Boolean,
    private val now: () -> Date = { Date() },
) {

    private val logger by taggedLogger("Chat:PendingMessagePins")

    private val pending = ConcurrentHashMap<String, PendingPin>()

    /**
     * Handles the result of a pin or unpin of [message]. A temporary network error without a network applies the
     * change locally and keeps it to send again. A success drops a change kept before, so it is not sent over this
     * newer one.
     */
    fun onPinResult(message: Message, pinned: Boolean, pinExpires: Date?, result: Result<Message>) {
        when (result) {
            is Result.Success -> pending.remove(message.id)
            is Result.Failure -> if (!isNetworkAvailable() && result.value.isTemporaryNetworkError()) {
                val current = logic.getMessageById(message.id) ?: message
                val change = PinFields(
                    pinned = pinned,
                    pinnedAt = now().takeIf { pinned },
                    pinExpires = pinExpires.takeIf { pinned },
                    pinnedBy = getCurrentUser().takeIf { pinned },
                )
                synchronized(pending) {
                    val original = pending[message.id]?.original ?: PinFields.of(current)
                    pending[message.id] = PendingPin(change = change, original = original)
                }
                logger.d { "[onPinResult] kept to send again, messageId: ${message.id}, pinned: $pinned" }
                upsert(current.with(change))
            }
        }
    }

    /**
     * Sends the kept pins and unpins again. A change that fails with an error other than a temporary network error
     * is dropped, and the message shows its pin state from before the change.
     */
    suspend fun sendPending() {
        pending.forEach { (messageId, pendingPin) ->
            val result = sendPin(messageId, pendingPin.change.toSet())
            if (result is Result.Failure && result.value.isTemporaryNetworkError()) return@forEach
            // A newer pin or unpin made while this one was sent stays kept.
            if (!pending.remove(messageId, pendingPin)) return@forEach
            if (result is Result.Failure) {
                logger.e { "[sendPending] dropped, messageId: $messageId, error: ${result.value}" }
                logic.getMessageById(messageId)?.let { upsert(it.with(pendingPin.original)) }
            }
        }
    }

    private fun upsert(message: Message) {
        logic.channelFromMessage(message)?.upsertMessage(message)
        logic.getActiveQueryThreadsLogic().forEach { it.upsertMessage(message) }
        logic.threadFromMessage(message)?.stateLogic()?.upsertMessage(message)
    }

    private fun Error.isTemporaryNetworkError(): Boolean = this is Error.NetworkError && !isPermanent()

    private class PendingPin(val change: PinFields, val original: PinFields)

    private data class PinFields(
        val pinned: Boolean,
        val pinnedAt: Date?,
        val pinExpires: Date?,
        val pinnedBy: User?,
    ) {
        fun toSet(): Map<String, Any> = buildMap {
            put("pinned", pinned)
            pinExpires?.let { put("pin_expires", it) }
        }

        companion object {
            fun of(message: Message) = PinFields(
                pinned = message.pinned,
                pinnedAt = message.pinnedAt,
                pinExpires = message.pinExpires,
                pinnedBy = message.pinnedBy,
            )
        }
    }

    private fun Message.with(fields: PinFields): Message = copy(
        pinned = fields.pinned,
        pinnedAt = fields.pinnedAt,
        pinExpires = fields.pinExpires,
        pinnedBy = fields.pinnedBy,
    )
}
