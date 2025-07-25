/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.state.plugin.listener.internal

import io.getstream.chat.android.client.plugin.listeners.LiveLocationListener
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.result.Result

internal class LiveLocationListenerState(
    private val mutableGlobalState: MutableGlobalState,
) : LiveLocationListener {
    override suspend fun onUpdateLiveLocationPrecondition(location: Location): Result<Unit> = Result.Success(Unit)

    override suspend fun onQueryActiveLocationsResult(result: Result<List<Location>>) {
        result.onSuccess { mutableGlobalState.addLiveLocations(it) }
    }

    override suspend fun onUpdateLiveLocationResult(location: Location, result: Result<Location>) {
        result.onSuccess { mutableGlobalState.addLiveLocation(it) }
    }

    override suspend fun onStopLiveLocationSharingResult(location: Location, result: Result<Location>) {
        result.onSuccess { mutableGlobalState.removeExpiredLiveLocations() }
    }

    override suspend fun onStartLiveLocationSharingResult(location: Location, result: Result<Location>) {
        result.onSuccess { mutableGlobalState.addLiveLocation(it) }
    }
}
