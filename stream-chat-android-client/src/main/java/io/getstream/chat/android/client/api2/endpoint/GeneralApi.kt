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
import io.getstream.chat.android.client.api2.model.dto.UnreadDto
import io.getstream.chat.android.client.api2.model.requests.QueryMembersRequest
import io.getstream.chat.android.client.api2.model.requests.SearchMessagesRequest
import io.getstream.chat.android.client.api2.model.requests.SyncHistoryRequest
import io.getstream.chat.android.client.api2.model.response.QueryMembersResponse
import io.getstream.chat.android.client.api2.model.response.SearchMessagesResponse
import io.getstream.chat.android.client.api2.model.response.SyncHistoryResponse
import io.getstream.chat.android.client.call.RetrofitCall
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.OPTIONS
import retrofit2.http.POST
import retrofit2.http.Query

@AuthenticatedApi
internal interface GeneralApi {
    @OPTIONS("/connect")
    fun warmUp(): RetrofitCall<ResponseBody>

    @POST("/sync")
    fun getSyncHistory(
        @Body body: SyncHistoryRequest,
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
    ): RetrofitCall<SyncHistoryResponse>

    @GET("/search")
    fun searchMessages(
        @UrlQueryPayload @Query("payload") payload: SearchMessagesRequest,
    ): RetrofitCall<SearchMessagesResponse>

    @GET("/members")
    fun queryMembers(
        @UrlQueryPayload @Query("payload") payload: QueryMembersRequest,
    ): RetrofitCall<QueryMembersResponse>

    @GET("/unread")
    fun getUnreadCounts(): RetrofitCall<UnreadDto>
}
