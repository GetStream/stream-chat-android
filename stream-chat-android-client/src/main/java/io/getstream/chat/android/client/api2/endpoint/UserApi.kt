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

package io.getstream.chat.android.client.api2.endpoint

import io.getstream.chat.android.client.api.AuthenticatedApi
import io.getstream.chat.android.client.api.QueryParams
import io.getstream.chat.android.client.api2.UrlQueryPayload
import io.getstream.chat.android.client.call.RetrofitCall
import io.getstream.chat.android.network.models.BlockUsersRequest
import io.getstream.chat.android.network.models.BlockUsersResponse
import io.getstream.chat.android.network.models.GetBlockedUsersResponse
import io.getstream.chat.android.network.models.QueryUsersPayload
import io.getstream.chat.android.network.models.QueryUsersResponse
import io.getstream.chat.android.network.models.SharedLocationResponse
import io.getstream.chat.android.network.models.SharedLocationsResponse
import io.getstream.chat.android.network.models.UnblockUsersRequest
import io.getstream.chat.android.network.models.UnblockUsersResponse
import io.getstream.chat.android.network.models.UpdateLiveLocationRequest
import io.getstream.chat.android.network.models.UpdateUsersPartialRequest
import io.getstream.chat.android.network.models.UpdateUsersRequest
import io.getstream.chat.android.network.models.UpdateUsersResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

@AuthenticatedApi
internal interface UserApi {

    @POST("/api/v2/users")
    fun updateUsers(
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body body: UpdateUsersRequest,
    ): RetrofitCall<UpdateUsersResponse>

    @POST("/api/v2/users/block")
    @JvmSuppressWildcards
    fun blockUser(@Body body: BlockUsersRequest): RetrofitCall<BlockUsersResponse>

    @POST("/api/v2/users/unblock")
    fun unblockUser(@Body body: UnblockUsersRequest): RetrofitCall<UnblockUsersResponse>

    @GET("/api/v2/users/block")
    fun queryBlockedUsers(): RetrofitCall<GetBlockedUsersResponse>

    @PATCH("/api/v2/users")
    @JvmSuppressWildcards // See issue: https://github.com/square/retrofit/issues/3275
    fun partialUpdateUsers(
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body body: UpdateUsersPartialRequest,
    ): RetrofitCall<UpdateUsersResponse>

    @GET("/api/v2/users")
    fun queryUsers(
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @UrlQueryPayload @Query("payload") payload: QueryUsersPayload,
    ): RetrofitCall<QueryUsersResponse>

    @GET("/api/v2/users/live_locations")
    fun liveLocations(): RetrofitCall<SharedLocationsResponse>

    @PUT("/api/v2/users/live_locations")
    fun updateLiveLocation(
        @Body body: UpdateLiveLocationRequest,
    ): RetrofitCall<SharedLocationResponse>
}
