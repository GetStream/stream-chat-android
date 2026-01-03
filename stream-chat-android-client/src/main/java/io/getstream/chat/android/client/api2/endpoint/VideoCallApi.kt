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
import io.getstream.chat.android.client.api2.model.requests.VideoCallCreateRequest
import io.getstream.chat.android.client.api2.model.requests.VideoCallTokenRequest
import io.getstream.chat.android.client.api2.model.response.CreateVideoCallResponse
import io.getstream.chat.android.client.api2.model.response.VideoCallTokenResponse
import io.getstream.chat.android.client.call.RetrofitCall
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

@AuthenticatedApi
internal interface VideoCallApi {

    @Deprecated(
        "This third-party library integration is deprecated. Contact the support team for more information.",
        level = DeprecationLevel.WARNING,
    )
    @POST("/channels/{channelType}/{channelId}/call")
    fun createCall(
        @Path("channelType") channelType: String,
        @Path("channelId") channelId: String,
        @Body request: VideoCallCreateRequest,
    ): RetrofitCall<CreateVideoCallResponse>

    @Deprecated(
        "This third-party library integration is deprecated. Contact the support team for more information.",
        level = DeprecationLevel.WARNING,
    )
    @POST("/calls/{callId}")
    fun getCallToken(
        @Path("callId") callId: String,
        @Body request: VideoCallTokenRequest,
    ): RetrofitCall<VideoCallTokenResponse>
}
