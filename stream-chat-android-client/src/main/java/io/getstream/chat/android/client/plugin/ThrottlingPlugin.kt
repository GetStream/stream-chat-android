/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

internal class ThrottlingPlugin(
    private val now: () -> Long = { System.currentTimeMillis() },
) : Plugin {
    private val logger by taggedLogger("Chat:ThrottlingPlugin")
    private val lastMarkReadMap: MutableMap<String, Long> = mutableMapOf()
    private val liveLocationMap: MutableMap<String, Long> = mutableMapOf()

    override suspend fun onUpdateLiveLocationPrecondition(location: Location): Result<Unit> =
        checkThrottling(
            lastUpdateProvider = liveLocationMap,
            key = location.messageId,
            throttleMs = LIVE_LOCATION_THROTTLE_MS,
        ) {
            logger.w { "[onUpdateLiveLocationPrecondition] live location update is ignored (${location.messageId})" }
            Error.GenericError("Live location update throttled")
        }

    private fun checkThrottling(
        lastUpdateProvider: MutableMap<String, Long>,
        key: String,
        throttleMs: Long,
        failureGenerator: () -> Error.GenericError,
    ): Result<Unit> {
        val now = now()
        val lastUpdateAt = lastUpdateProvider[key] ?: 0
        val deltaLastUpdateAt = now - lastUpdateAt

        return when {
            deltaLastUpdateAt == now || deltaLastUpdateAt >= throttleMs ->
                Result.Success(Unit).also { lastUpdateProvider[key] = now }

            else ->
                Result.Failure(failureGenerator())
        }
    }

    override fun onUserDisconnected() {
        lastMarkReadMap.clear()
        liveLocationMap.clear()
    }
}

private const val LIVE_LOCATION_THROTTLE_MS = 3000L
