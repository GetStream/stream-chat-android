/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.api2.endpoint.MessageApi
import io.getstream.chat.android.client.api2.model.dto.UpstreamMessageDto

/**
 * Used to form an update message request.
 * @see [MessageApi.updateMessage]
 *
 * @param message The upstream version of the message.
 * @param skip_enrich_url If the message should skip enriching the URL. If URl is not enriched, it will not be
 * displayed as a link attachment. False by default.
 * @param skip_push If the update of the message should skip sending a push notification. False by default.
 * */
@JsonClass(generateAdapter = true)
internal data class UpdateMessageRequest(
    val message: UpstreamMessageDto,
    val skip_enrich_url: Boolean = false,
    val skip_push: Boolean = false,
)
