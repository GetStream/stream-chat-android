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
import io.getstream.chat.android.client.api2.model.response.CompletableResponse
import io.getstream.chat.android.client.api2.model.response.UserGroupResponse
import io.getstream.chat.android.client.api2.model.response.UserGroupsResponse
import io.getstream.chat.android.client.call.RetrofitCall
import io.getstream.chat.android.network.models.AddUserGroupMembersRequest
import io.getstream.chat.android.network.models.CreateUserGroupRequest
import io.getstream.chat.android.network.models.RemoveUserGroupMembersRequest
import io.getstream.chat.android.network.models.UpdateUserGroupRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

@AuthenticatedApi
internal interface UserGroupApi {

    @POST("/usergroups")
    fun createUserGroup(@Body body: CreateUserGroupRequest): RetrofitCall<UserGroupResponse>

    @GET("/usergroups")
    fun queryUserGroups(
        @Query("limit") limit: Int? = null,
        @Query("id_gt") idGt: String? = null,
        @Query("created_at_gt") createdAtGt: String? = null,
        @Query("team_id") teamId: String? = null,
    ): RetrofitCall<UserGroupsResponse>

    @GET("/usergroups/search")
    fun searchUserGroups(
        @Query("query") query: String,
        @Query("limit") limit: Int? = null,
        @Query("team_id") teamId: String? = null,
        @Query("name_gt") nameGt: String? = null,
        @Query("id_gt") idGt: String? = null,
    ): RetrofitCall<UserGroupsResponse>

    @GET("/usergroups/{id}")
    fun getUserGroup(
        @Path("id") id: String,
        @Query("team_id") teamId: String? = null,
    ): RetrofitCall<UserGroupResponse>

    @PUT("/usergroups/{id}")
    fun updateUserGroup(
        @Path("id") id: String,
        @Body body: UpdateUserGroupRequest,
    ): RetrofitCall<UserGroupResponse>

    @DELETE("/usergroups/{id}")
    fun deleteUserGroup(
        @Path("id") id: String,
        @Query("team_id") teamId: String? = null,
    ): RetrofitCall<CompletableResponse>

    @POST("/usergroups/{id}/members")
    fun addUserGroupMembers(
        @Path("id") id: String,
        @Body body: AddUserGroupMembersRequest,
    ): RetrofitCall<UserGroupResponse>

    @POST("/usergroups/{id}/members/delete")
    fun removeUserGroupMembers(
        @Path("id") id: String,
        @Body body: RemoveUserGroupMembersRequest,
    ): RetrofitCall<UserGroupResponse>
}
