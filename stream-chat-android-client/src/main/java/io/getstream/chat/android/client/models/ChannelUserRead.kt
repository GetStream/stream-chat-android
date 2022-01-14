package io.getstream.chat.android.client.models

import java.util.Date

/**
 * Information about how many messages are unread in the channel by a given user
 *
 * @property user - The user which has read some of the message and may have some unread messages
 * @property lastRead - The time of the last read message
 * @property unreadMessages - How many messages are unread
 * @property lastMessageSeenDate - The time of the last message that the SDK is aware of. If new messages arrive the the createdAt newer than this one, that means that the count of unread messages should be incremented.
 */
public data class ChannelUserRead(
    override var user: User,
    var lastRead: Date? = null,
    var unreadMessages: Int = 0,
    var lastMessageSeenDate: Date? = null
) : UserEntity
