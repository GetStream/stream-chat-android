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

package io.getstream.chat.android.client.internal.offline.repository.domain.message.channelinfo.internal

import io.getstream.chat.android.models.ChannelInfo

internal fun ChannelInfo.toEntity(): ChannelInfoEntity = ChannelInfoEntity(
    cid = cid,
    id = id,
    type = type,
    memberCount = memberCount,
    name = name,
)

internal fun ChannelInfoEntity.toModel(): ChannelInfo = ChannelInfo(
    cid = cid,
    id = id,
    type = type,
    memberCount = memberCount ?: 0,
    name = name,
)
