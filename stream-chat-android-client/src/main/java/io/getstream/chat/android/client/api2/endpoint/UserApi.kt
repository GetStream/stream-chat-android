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

package io.getstream.chat.android.client.api2.endpoint

import io.getstream.chat.android.client.api.AuthenticatedApi
import io.getstream.chat.android.client.api.QueryParams
import io.getstream.chat.android.client.api2.UrlQueryPayload
import io.getstream.chat.android.client.api2.model.requests.BlockUserRequest
import io.getstream.chat.android.client.api2.model.requests.PartialUpdateUsersRequest
import io.getstream.chat.android.client.api2.model.requests.QueryUsersRequest
import io.getstream.chat.android.client.api2.model.requests.UnblockUserRequest
import io.getstream.chat.android.client.api2.model.requests.UpdateUsersRequest
import io.getstream.chat.android.client.api2.model.response.BlockUserResponse
import io.getstream.chat.android.client.api2.model.response.QueryBlockedUsersResponse
import io.getstream.chat.android.client.api2.model.response.UnblockUserResponse
import io.getstream.chat.android.client.api2.model.response.UpdateUsersResponse
import io.getstream.chat.android.client.api2.model.response.UsersResponse
import io.getstream.chat.android.client.call.RetrofitCall
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

@AuthenticatedApi
internal interface UserApi {
    @POST("/users")
    fun updateUsers(
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body body: UpdateUsersRequest,
    ): RetrofitCall<UpdateUsersResponse>

    @POST("/users/block")
    @JvmSuppressWildcards
    fun blockUser(@Body body: BlockUserRequest): RetrofitCall<BlockUserResponse>

    @POST("/users/unblock")
    fun unblockUser(@Body body: UnblockUserRequest): RetrofitCall<UnblockUserResponse>

    @GET("/users/block")
    fun queryBlockedUsers(): RetrofitCall<QueryBlockedUsersResponse>

    @PATCH("/users")
    @JvmSuppressWildcards // See issue: https://github.com/square/retrofit/issues/3275
    fun partialUpdateUsers(
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body body: PartialUpdateUsersRequest,
    ): RetrofitCall<UpdateUsersResponse>

    @GET("/users")
    fun queryUsers(
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @UrlQueryPayload @Query("payload") payload: QueryUsersRequest,
    ): RetrofitCall<UsersResponse>
}
