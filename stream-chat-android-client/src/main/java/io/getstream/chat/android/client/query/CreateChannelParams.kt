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

package io.getstream.chat.android.client.query

import io.getstream.chat.android.models.MemberData

/**
 * Model holding data for creating a channel.
 *
 * @param members The list of the members with extra data to be added to the channel.
 * @param extraData Map of key-value pairs that let you store extra data.
 */
public data class CreateChannelParams(
    val members: List<MemberData>,
    val extraData: Map<String, Any>,
) {

    /**
     * Ids of the members to be added to the channel.
     */
    val memberIds: List<String> = members.map(MemberData::userId)
}
