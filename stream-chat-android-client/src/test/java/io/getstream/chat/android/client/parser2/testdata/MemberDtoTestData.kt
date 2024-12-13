/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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
import io.getstream.chat.android.client.api2.model.dto.UpstreamMemberDto
import org.intellij.lang.annotations.Language

internal object MemberDtoTestData {

    @Language("JSON")
    val downstreamJsonWithExtraData = """
        {
        "user" : ${UserDtoTestData.downstreamJson},
        "shadow_banned" : false,
        "banned" : false,
        "is_premium" : true
        }""".withoutWhitespace()

    @Language("JSON")
    val downstreamJsonWithoutExtraData = """
        {
        "user" : ${UserDtoTestData.downstreamJson},
        "shadow_banned" : false,
        "banned" : false
        }""".withoutWhitespace()

    @Language("JSON")
    val upstreamJsonWithExtraData = """
        {
        "user" : ${UserDtoTestData.upstreamJson},
        "shadow_banned" : false,
        "banned" : false,
        "is_premium" : true
        }""".withoutWhitespace()

    @Language("JSON")
    val upstreamJsonWithoutExtraData = """
        {
        "user" : ${UserDtoTestData.upstreamJson},
        "shadow_banned" : false,
        "banned" : false
        }""".withoutWhitespace()

    val downstreamMemberWithExtraData = DownstreamMemberDto(
        user = UserDtoTestData.downstreamUser,
        created_at = null,
        updated_at = null,
        invited = null,
        invite_accepted_at = null,
        invite_rejected_at = null,
        shadow_banned = false,
        banned = false,
        channel_role = null,
        notifications_muted = null,
        status = null,
        ban_expires = null,
        pinned_at = null,
        archived_at = null,
        extraData = mapOf("is_premium" to true),
    )

    val downstreamMemberWithoutExtraData = DownstreamMemberDto(
        user = UserDtoTestData.downstreamUser,
        created_at = null,
        updated_at = null,
        invited = null,
        invite_accepted_at = null,
        invite_rejected_at = null,
        shadow_banned = false,
        banned = false,
        channel_role = null,
        notifications_muted = null,
        status = null,
        ban_expires = null,
        pinned_at = null,
        archived_at = null,
        extraData = emptyMap(),
    )

    val upstreamMemberWithExtraData = UpstreamMemberDto(
        user = UserDtoTestData.upstreamUser,
        created_at = null,
        updated_at = null,
        invited = null,
        invite_accepted_at = null,
        invite_rejected_at = null,
        shadow_banned = false,
        banned = false,
        channel_role = null,
        notifications_muted = null,
        status = null,
        ban_expires = null,
        pinned_at = null,
        archived_at = null,
        extraData = mapOf("is_premium" to true),
    )

    val upstreamMemberWithoutExtraData = UpstreamMemberDto(
        user = UserDtoTestData.upstreamUser,
        created_at = null,
        updated_at = null,
        invited = null,
        invite_accepted_at = null,
        invite_rejected_at = null,
        shadow_banned = false,
        banned = false,
        channel_role = null,
        notifications_muted = null,
        status = null,
        ban_expires = null,
        pinned_at = null,
        archived_at = null,
        extraData = emptyMap(),
    )
}
