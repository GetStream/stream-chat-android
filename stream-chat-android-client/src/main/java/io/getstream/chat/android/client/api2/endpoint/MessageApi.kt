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
import io.getstream.chat.android.network.models.GetRepliesResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

@AuthenticatedApi
internal interface MessageApi {

    // The generated getReplies takes the sort as a list of objects, which the query converter can't encode yet.
    @GET("/api/v2/chat/messages/{parent_id}/replies?sort=[{\"field\":\"created_at\",\"direction\":1}]")
    fun getNewerReplies(
        @Path("parent_id") parentId: String,
        @Query("limit") limit: Int,
        @Query("id_gt") lastId: String?,
    ): RetrofitCall<GetRepliesResponse>
}
