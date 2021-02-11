package io.getstream.chat.android.ui

import io.getstream.chat.android.client.models.ChannelMute
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.test.positiveRandomInt
import io.getstream.chat.android.test.randomBoolean
import io.getstream.chat.android.test.randomDate
import io.getstream.chat.android.test.randomString
import java.util.Date

internal fun createUser(
    id: String = randomString(),
    role: String = randomString(),
    invisible: Boolean = randomBoolean(),
    banned: Boolean = randomBoolean(),
    devices: List<Device> = mutableListOf(),
    online: Boolean = randomBoolean(),
    createdAt: Date? = randomDate(),
    updatedAt: Date? = randomDate(),
    lastActive: Date? = randomDate(),
    totalUnreadCount: Int = positiveRandomInt(),
    unreadChannels: Int = positiveRandomInt(),
    unreadCount: Int = positiveRandomInt(),
    mutes: List<Mute> = mutableListOf(),
    teams: List<String> = listOf(),
    channelMutes: List<ChannelMute> = emptyList(),
    extraData: MutableMap<String, Any> = mutableMapOf()
): User = User(
    id,
    role,
    invisible,
    banned,
    devices,
    online,
    createdAt,
    updatedAt,
    lastActive,
    totalUnreadCount,
    unreadChannels,
    unreadCount,
    mutes,
    teams,
    channelMutes,
    extraData
)

internal fun createMember(
    user: User = createUser(),
    role: String = randomString(),
    createdAt: Date? = randomDate(),
    updatedAt: Date? = randomDate(),
    isInvited: Boolean = randomBoolean(),
    inviteAcceptedAt: Date? = randomDate(),
    inviteRejectedAt: Date? = randomDate()
): Member = Member(user, role, createdAt, updatedAt, isInvited, inviteAcceptedAt, inviteRejectedAt)

internal fun createMembers(
    size: Int = positiveRandomInt(10),
    creationFunction: (Int) -> Member = { createMember() }
): List<Member> = List(size, creationFunction)
