/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.plugin.listeners

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.Location
import io.getstream.result.Result

/**
 * Listener for [ChatClient.updateLiveLocation] requests.
 */
public interface LiveLocationListener {

    /**
     * Run precondition for the request. If it returns [Result.Success] then the request is run otherwise it returns
     * [Result.Failure] and no request is made.
     *
     * @param location The location to be updated.
     *
     * @return [Result.Success] if precondition passes, otherwise [Result.Failure].
     */
    public suspend fun onUpdateLiveLocationPrecondition(location: Location): Result<Unit>

    /**
     * Called when the result of [ChatClient.queryActiveLocations] is received.
     *
     * @param result The result of the query, which is a list of active locations.
     */
    public suspend fun onQueryActiveLocationsResult(result: Result<List<Location>>)

    /**
     * Called when the result of [ChatClient.updateLiveLocation] is received.
     *
     * @param location The location used in the request.
     * @param result The result of the request.
     */
    public suspend fun onUpdateLiveLocationResult(location: Location, result: Result<Location>)

    /**
     * Called when the result of [ChatClient.stopLiveLocationSharing] is received.
     *
     * @param location The location used in request.
     * @param result The result of the request.
     */
    public suspend fun onStopLiveLocationSharingResult(location: Location, result: Result<Location>)

    /**
     * Called when the result of [ChatClient.startLiveLocationSharing] is received.
     *
     * @param location The location used in the request.
     * @param result The result of the request.
     */
    public suspend fun onStartLiveLocationSharingResult(location: Location, result: Result<Location>)
}
