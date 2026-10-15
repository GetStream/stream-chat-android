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
import io.getstream.chat.android.client.call.RetrofitCall
import io.getstream.chat.android.network.models.CreateDraftRequest
import io.getstream.chat.android.network.models.CreateDraftResponse
import io.getstream.chat.android.network.models.QueryDraftsRequest
import io.getstream.chat.android.network.models.QueryDraftsResponse
import io.getstream.chat.android.network.models.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

@AuthenticatedApi
internal interface DraftApi {

    @POST("/api/v2/chat/channels/{type}/{id}/draft")
    fun createDraftMessage(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body request: CreateDraftRequest,
    ): RetrofitCall<CreateDraftResponse>

    @DELETE("/api/v2/chat/channels/{type}/{id}/draft")
    fun deleteDraftMessage(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query("parent_id") parentId: String?,
    ): RetrofitCall<Response>

    /**
     * Queries draft messages for the current user.
     */
    @POST("/api/v2/chat/drafts/query")
    fun queryDrafts(@Body body: QueryDraftsRequest): RetrofitCall<QueryDraftsResponse>
}
