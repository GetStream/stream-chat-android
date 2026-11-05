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

package io.getstream.chat.android.client.plugin

import io.getstream.chat.android.models.Location
import io.getstream.log.taggedLogger
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Sends at most one live location update per message every [LIVE_LOCATION_THROTTLE_MS]. An update arriving
 * inside the window is held until the window ends and then sent, unless a newer update, a successful stop or a
 * disconnect drops it first; a dropped update fails at once.
 *
 * @param now Monotonic clock in milliseconds.
 */
internal class ThrottlingPlugin(
    private val now: () -> Long = { System.nanoTime() / NANOS_PER_MILLI },
) : Plugin {
    private val logger by taggedLogger("Chat:ThrottlingPlugin")
    private val lock = Any()
    private val lastLiveLocationSentAt = mutableMapOf<String, Long>()

    // One entry per held invocation; completing it drops that invocation.
    private val heldLiveLocations = mutableMapOf<String, CompletableDeferred<Unit>>()

    override suspend fun onUpdateLiveLocationPrecondition(location: Location): Result<Unit> {
        val messageId = location.messageId
        val ticket = CompletableDeferred<Unit>()
        val waitMs = synchronized(lock) {
            val now = now()
            val remaining = lastLiveLocationSentAt[messageId]
                ?.let { LIVE_LOCATION_THROTTLE_MS - (now - it) }
                ?: 0L
            heldLiveLocations.remove(messageId)?.complete(Unit)
            if (remaining <= 0L) {
                lastLiveLocationSentAt[messageId] = now
            } else {
                heldLiveLocations[messageId] = ticket
            }
            remaining
        }
        if (waitMs <= 0L) return Result.Success(Unit)

        try {
            withTimeoutOrNull(waitMs) { ticket.await() }
        } catch (e: CancellationException) {
            synchronized(lock) { heldLiveLocations.removeIfSame(messageId, ticket) }
            throw e
        }
        return synchronized(lock) {
            if (heldLiveLocations.removeIfSame(messageId, ticket)) {
                lastLiveLocationSentAt[messageId] = now()
                Result.Success(Unit)
            } else {
                logger.d { "[onUpdateLiveLocationPrecondition] dropped held update ($messageId)" }
                Result.Failure(
                    Error.GenericError("Live location update dropped by a newer update, a stop or a disconnect"),
                )
            }
        }
    }

    override suspend fun onStopLiveLocationSharingResult(location: Location, result: Result<Location>) {
        if (result is Result.Success) {
            synchronized(lock) {
                heldLiveLocations.remove(location.messageId)?.complete(Unit)
                lastLiveLocationSentAt.remove(location.messageId)
            }
        }
    }

    override fun onUserDisconnected() {
        synchronized(lock) {
            heldLiveLocations.values.forEach { it.complete(Unit) }
            heldLiveLocations.clear()
            lastLiveLocationSentAt.clear()
        }
    }

    private fun <K, V> MutableMap<K, V>.removeIfSame(key: K, value: V): Boolean =
        (get(key) === value).also { if (it) remove(key) }
}

private const val NANOS_PER_MILLI = 1_000_000L

private const val LIVE_LOCATION_THROTTLE_MS = 3000L
