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
import io.getstream.chat.android.client.api2.model.response.EventResponse
import io.getstream.chat.android.client.call.RetrofitCall
import io.getstream.chat.android.network.models.GroupedQueryChannelsRequest
import io.getstream.chat.android.network.models.GroupedQueryChannelsResponse
import io.getstream.chat.android.network.models.SendEventRequest
import io.getstream.chat.android.network.models.UpdateMemberPartialRequest
import io.getstream.chat.android.network.models.UpdateMemberPartialResponse
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Channel endpoints that stay on v1. Sending an event returns a hand-written v1 model. The member update keeps the
 * user id path parameter its public API takes, which the v2 route doesn't have. Grouped queries are enabled only for
 * some apps, so their v2 response can't be verified yet.
 */
@AuthenticatedApi
internal interface ChannelV1Api {

    /**
     * Queries channels grouped into server-defined groups.
     *
     * Supports per-group request options (limit, next/prev cursors) and returns per-group
     * pagination cursors. Pagination is only allowed when exactly one group is requested.
     *
     * @param connectionId The current connection ID.
     * @param body The request body containing the optional per-group configuration map.
     */
    @POST("/channels/grouped")
    fun queryGroupedChannels(
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body body: GroupedQueryChannelsRequest,
    ): RetrofitCall<GroupedQueryChannelsResponse>

    @PATCH("/channels/{type}/{id}/member/{user_id}")
    fun partialUpdateMember(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Path("user_id") userId: String,
        @Body body: UpdateMemberPartialRequest,
    ): RetrofitCall<UpdateMemberPartialResponse>

    @POST("/channels/{type}/{id}/event")
    fun sendEvent(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body request: SendEventRequest,
    ): RetrofitCall<EventResponse>
}
