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
import io.getstream.chat.android.client.api2.model.requests.FlagRequest
import io.getstream.chat.android.client.api2.model.response.FlagResponse
import io.getstream.chat.android.client.call.RetrofitCall
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Flag endpoints that stay on v1. The v2 flag returns no flag record and sends no `message.flagged` or `user.flagged`
 * event, while this one does both. Unflag has no v2 route.
 */
@AuthenticatedApi
internal interface ModerationV1Api {

    @POST("/moderation/flag")
    fun flag(@Body body: FlagRequest): RetrofitCall<FlagResponse>

    @POST("/moderation/unflag")
    fun unflag(@Body body: Map<String, String>): RetrofitCall<FlagResponse>
}
