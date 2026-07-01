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

package io.getstream.chat.android.client.parser2.testdata

import io.getstream.chat.android.client.api2.model.dto.DownstreamMemberDto
import org.intellij.lang.annotations.Language

internal object MemberDtoTestData {

    @Language("JSON")
    val downstreamJsonWithExtraData = """
        {
        "user" : ${UserDtoTestData.downstreamJson},
        "created_at" : "2020-06-10T11:04:31.000Z",
        "updated_at" : "2020-06-10T11:04:31.588Z",
        "channel_role" : "channel_member",
        "notifications_muted" : false,
        "shadow_banned" : false,
        "banned" : false,
        "is_premium" : true
        }""".withoutWhitespace()

    @Language("JSON")
    val downstreamJsonWithoutExtraData = """
        {
        "user" : ${UserDtoTestData.downstreamJson},
        "created_at" : "2020-06-10T11:04:31.000Z",
        "updated_at" : "2020-06-10T11:04:31.588Z",
        "channel_role" : "channel_member",
        "notifications_muted" : false,
        "shadow_banned" : false,
        "banned" : false
        }""".withoutWhitespace()

    val downstreamMemberWithExtraData = DownstreamMemberDto(
        user = UserDtoTestData.downstreamUser,
        createdAt = java.util.Date(1591787071000),
        updatedAt = java.util.Date(1591787071588),
        channelRole = "channel_member",
        notificationsMuted = false,
        shadowBanned = false,
        banned = false,
        custom = mapOf("is_premium" to true),
    )

    val downstreamMemberWithoutExtraData = DownstreamMemberDto(
        user = UserDtoTestData.downstreamUser,
        createdAt = java.util.Date(1591787071000),
        updatedAt = java.util.Date(1591787071588),
        channelRole = "channel_member",
        notificationsMuted = false,
        shadowBanned = false,
        banned = false,
        custom = emptyMap(),
    )
}
