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
import io.getstream.chat.android.client.api2.model.response.SearchRolesResponse
import io.getstream.chat.android.client.call.RetrofitCall
import retrofit2.http.GET
import retrofit2.http.Query

@AuthenticatedApi
internal interface RoleApi {

    @GET("/roles/search")
    fun searchRoles(
        @Query("query") query: String,
        @Query("limit") limit: Int? = null,
        @Query("role_type") roleType: String? = null,
        @Query("include_global_roles") includeGlobalRoles: Boolean? = null,
        @Query("name_gt") nameGt: String? = null,
    ): RetrofitCall<SearchRolesResponse>
}
