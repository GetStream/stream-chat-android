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

package io.getstream.chat.android.client.cdn

/**
 * Model representing the response for a successfully uploaded file to a CDN.
 *
 * @param url Url of the uploaded file.
 * @param thumbnailUrl Url of the thumbnail for the uploaded file.
 * @param extraData Additional data related to the uploaded file.
 */
public data class CDNResponse(
    val url: String,
    val thumbnailUrl: String? = null,
    val extraData: Map<String, Any>? = null,
)
