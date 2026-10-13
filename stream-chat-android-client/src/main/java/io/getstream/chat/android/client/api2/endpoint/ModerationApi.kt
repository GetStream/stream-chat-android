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
import io.getstream.chat.android.client.api2.UrlQueryPayload
import io.getstream.chat.android.client.call.RetrofitCall
import io.getstream.chat.android.network.models.BanRequest
import io.getstream.chat.android.network.models.ModerationBanResponse
import io.getstream.chat.android.network.models.MuteChannelRequest
import io.getstream.chat.android.network.models.MuteRequest
import io.getstream.chat.android.network.models.MuteResponse
import io.getstream.chat.android.network.models.QueryBannedUsersPayload
import io.getstream.chat.android.network.models.QueryBannedUsersResponse
import io.getstream.chat.android.network.models.Response
import io.getstream.chat.android.network.models.UnbanResponse
import io.getstream.chat.android.network.models.UnmuteRequest
import io.getstream.chat.android.network.models.UnmuteResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

@AuthenticatedApi
internal interface ModerationApi {

    @POST("/api/v2/moderation/mute")
    fun mute(@Body body: MuteRequest): RetrofitCall<MuteResponse>

    @POST("/api/v2/moderation/unmute")
    fun unmute(@Body body: UnmuteRequest): RetrofitCall<UnmuteResponse>

    @POST("/api/v2/moderation/ban")
    fun ban(@Body body: BanRequest): RetrofitCall<ModerationBanResponse>

    @POST("/api/v2/moderation/unban")
    fun unban(
        @Query("target_user_id") targetUserId: String,
        @Query("channel_cid") channelCid: String,
    ): RetrofitCall<UnbanResponse>

    @POST("/api/v2/chat/moderation/mute/channel")
    fun muteChannel(@Body body: MuteChannelRequest): RetrofitCall<Response>

    @POST("/api/v2/chat/moderation/unmute/channel")
    fun unmuteChannel(@Body body: MuteChannelRequest): RetrofitCall<Response>

    @GET("/api/v2/chat/query_banned_users")
    fun queryBannedUsers(
        @UrlQueryPayload @Query("payload") payload: QueryBannedUsersPayload,
    ): RetrofitCall<QueryBannedUsersResponse>
}
