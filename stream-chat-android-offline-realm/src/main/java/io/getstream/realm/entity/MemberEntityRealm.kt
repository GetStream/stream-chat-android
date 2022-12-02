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

package io.getstream.realm.entity

import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.User
import io.getstream.realm.utils.toDate
import io.getstream.realm.utils.toRealmInstant
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

@Suppress("VariableNaming")
internal class MemberEntityRealm : RealmObject {
    @PrimaryKey
    var user_id: String = ""

    var user: UserEntityRealm? = null

    /** the user's role, user, moderator or admin */
    var role: String = ""

    /** when the user became a member */
    var created_at: RealmInstant? = null

    /** when the membership data was last updated */
    var updated_at: RealmInstant? = null

    /** if this is an invite */
    var is_invited: Boolean = false

    /** the date the invite was accepted */
    var invite_accepted_at: RealmInstant? = null

    /** the date the invite was rejected */
    var invite_rejected_at: RealmInstant? = null

    /** if channel member is shadow banned */
    var shadow_banned: Boolean = false

    /** If channel member is banned. */
    var banned: Boolean = false

    /** The user's channel-level role. */
    var channel_role: String? = null
}

internal fun Member.toRealm(): MemberEntityRealm {
    val thisMember = this

    return MemberEntityRealm().apply {
        user_id = thisMember.user.id
        user = thisMember.user.toRealm()
        created_at = thisMember.createdAt?.toRealmInstant()
        updated_at = thisMember.updatedAt?.toRealmInstant()
        is_invited = thisMember.isInvited ?: false
        invite_accepted_at = thisMember.inviteAcceptedAt?.toRealmInstant()
        invite_rejected_at = thisMember.inviteRejectedAt?.toRealmInstant()
        shadow_banned = thisMember.shadowBanned
        banned = thisMember.banned
        channel_role = thisMember.channelRole
    }
}

internal fun MemberEntityRealm.toDomain(): Member =
    Member(
        user = user?.toDomain() ?: User(),
        createdAt = created_at?.toDate(),
        updatedAt = updated_at?.toDate(),
        isInvited = is_invited,
        inviteAcceptedAt = invite_accepted_at?.toDate(),
        inviteRejectedAt = invite_rejected_at?.toDate(),
        shadowBanned = shadow_banned,
        banned = banned,
        channelRole = channel_role,
    )
