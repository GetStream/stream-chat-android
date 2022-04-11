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

package io.getstream.chat.android.client.models

import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * A [Channel] object that contains less information.
 * Used only internally.
 *
 * @param cid The channel id in the format messaging:123.
 * @param id Channel's unique ID.
 * @param type Type of the channel.
 * @param memberCount Number of members in the channel.
 * @param name Channel's name.
 * @param image Channel's image.
 */
@InternalStreamChatApi
public data class ChannelInfo(
    val cid: String? = null,
    val id: String? = null,
    val type: String? = null,
    val memberCount: Int = 0,
    val name: String? = null,
    val image: String? = null,
)
