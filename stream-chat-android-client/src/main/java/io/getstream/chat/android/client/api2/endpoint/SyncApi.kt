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
import io.getstream.chat.android.client.api2.model.requests.SyncHistoryRequest
import io.getstream.chat.android.client.api2.model.response.SyncHistoryResponse
import io.getstream.chat.android.client.call.RetrofitCall
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.OPTIONS
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Endpoints tied to the websocket connection that are still on v1. `/sync` replays events in the v1 encoding the v1
 * parser reads, whichever socket is connected; the `/connect` warm-up has no v2 route (the v2 socket is
 * `/api/v2/connect`).
 */
@AuthenticatedApi
internal interface SyncApi {

    @OPTIONS("/connect")
    fun warmUp(): RetrofitCall<ResponseBody>

    @POST("/sync")
    fun getSyncHistory(
        @Body body: SyncHistoryRequest,
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
    ): RetrofitCall<SyncHistoryResponse>
}
