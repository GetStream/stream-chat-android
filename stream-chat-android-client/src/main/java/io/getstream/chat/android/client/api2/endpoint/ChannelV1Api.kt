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
import io.getstream.chat.android.client.api2.model.response.EventResponse
import io.getstream.chat.android.client.call.RetrofitCall
import io.getstream.chat.android.network.models.SendEventRequest
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Channel endpoints that stay on v1. Sending an event returns a hand-written v1 model.
 */
@AuthenticatedApi
internal interface ChannelV1Api {

    @POST("/channels/{type}/{id}/event")
    fun sendEvent(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body request: SendEventRequest,
    ): RetrofitCall<EventResponse>
}
