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

package io.getstream.chat.android.internal.offline.repository.domain.message.channelinfo.internal

import io.getstream.chat.android.internal.offline.repository.domain.message.internal.MessageInnerEntity

/**
 * Channel information embedded within message.
 *
 * All the fields are nullable so that Room is able to distinguish when channel information is completely absent.
 * In that case, when embedded field is read ([MessageInnerEntity.channelInfo]), the embedded object is not
 * constructed and the reference is set to null.
 */
internal data class ChannelInfoEntity(
    val cid: String?,
    val id: String?,
    val type: String?,
    val memberCount: Int?,
    val name: String?,
)
